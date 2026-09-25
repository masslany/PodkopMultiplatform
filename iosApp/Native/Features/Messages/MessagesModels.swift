import Foundation
import Observation

@MainActor @Observable
final class InboxModel {
    let pager = ListPager<NativeConversation>(key: \.id)
    private let loader: MessagesLoading

    init(loader: MessagesLoading) { self.loader = loader }

    func start() {
        guard pager.phase == .idle else { return }
        let loader = loader
        pager.load(first: loader.firstRequest()) { try await loader.conversations(request: $0, loaded: $1) }
    }

    func refresh() async { await pager.refresh() }
    func stop() { pager.stop() }
}

/// Username entry with debounced suggestions; a conversation opens from a suggestion, as on Android.
@MainActor @Observable
final class NewConversationModel {
    enum Status: Equatable { case hidden, loading, loaded, failed }
    static let minimumQueryLength = 3
    var username = "" { didSet { if oldValue != username { schedule() } } }
    private(set) var suggestions: [NativeUserSuggestion] = []
    private(set) var status: Status = .hidden
    private let suggester: SearchSuggesting
    private let loader: MessagesLoading
    private let debounce: Duration
    private var task: Task<Void, Never>?
    private var revision = 0

    init(suggester: SearchSuggesting, loader: MessagesLoading, debounce: Duration = .milliseconds(300)) {
        self.suggester = suggester
        self.loader = loader
        self.debounce = debounce
    }

    var query: String { loader.normalize(username) }

    func retry() { schedule(immediately: true) }

    func stop() {
        revision += 1
        task?.cancel()
    }

    private func schedule(immediately: Bool = false) {
        revision += 1
        task?.cancel()
        let query = query
        // Android keeps the previous suggestions visible below the minimum length.
        guard query.count >= Self.minimumQueryLength else { return }
        let token = revision, delay = debounce, suggester = suggester
        task = Task { [weak self] in
            if !immediately { try? await Task.sleep(for: delay) }
            guard let self, !Task.isCancelled, token == self.revision else { return }
            self.status = .loading
            do {
                let values = try await suggester.users(query)
                guard token == self.revision else { return }
                self.suggestions = values
                self.status = .loaded
            } catch {
                guard token == self.revision, !(error is CancellationError) else { return }
                self.status = .failed
            }
        }
    }
}

/// Waits between polls; replaced in tests so polling runs without real time.
protocol PollClock {
    func sleep(seconds: Double) async throws
}

struct SystemPollClock: PollClock {
    func sleep(seconds: Double) async throws { try await Task.sleep(for: .seconds(seconds)) }
}

@MainActor @Observable
final class ConversationModel {
    enum Phase: Equatable { case loading, loaded, failed }
    static let pollInterval: Double = 30

    let username: String
    private(set) var phase: Phase = .loading
    private(set) var messages: [NativeMessage] = []
    private(set) var refreshing = false
    private(set) var olderLoading = false
    private(set) var olderFailed = false
    private(set) var hasOlder = false
    /// Incremented when the view should jump to the newest message (initial load, own send).
    private(set) var scrollToLatest = 0
    /// Set after older messages are prepended so the view can keep the previous first row in place.
    private(set) var anchorAfterPrepend: String?

    var text = ""
    var adult = false
    let attachment: ComposerAttachment
    private(set) var sending = false
    private(set) var sendFailed = false
    private(set) var outcomeUnknown = false

    private let loader: MessagesLoading
    private let clock: PollClock
    private var next: FeedRequest?
    private var seen = Set<FeedRequest>()
    private var generation = 0
    private var loadedOnce = false
    private var visible = false
    private var pollTask: Task<Void, Never>?
    private var loadTask: Task<Void, Never>?

    init(username: String, loader: MessagesLoading, media: ComposerMediaHandling?,
         clock: PollClock = SystemPollClock()) {
        self.username = username
        self.loader = loader
        self.clock = clock
        attachment = ComposerAttachment(media: media)
    }

    var isDirty: Bool {
        !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || adult || attachment.photoKey != nil
    }

    var canSend: Bool {
        !sending && !attachment.uploading && !outcomeUnknown &&
            (!text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || attachment.photoKey != nil)
    }

    var isPolling: Bool { pollTask != nil }

    // MARK: Lifecycle

    /// Visible and in the foreground: mark read, load or catch up, then poll every 30 seconds.
    func becameVisible() {
        guard !visible else { return }
        visible = true
        markRead()
        if !loadedOnce || (phase == .failed && messages.isEmpty) {
            load(scrollToLatest: true)
        } else {
            refreshNewer()
            startPolling()
        }
    }

    /// Hidden, backgrounded or signed out: stop polling; loaded messages are kept.
    func becameHidden() {
        visible = false
        stopPolling()
    }

    func retry() { load(scrollToLatest: true) }

    func refresh() async {
        load(scrollToLatest: messages.isEmpty, refreshingOnly: true)
        await loadTask?.value
    }

    // MARK: Loading

    private func load(scrollToLatest: Bool, refreshingOnly: Bool = false) {
        generation += 1
        let token = generation, loader = loader, username = username
        if refreshingOnly && !messages.isEmpty { refreshing = true } else { phase = .loading }
        let first = loader.firstRequest()
        loadTask?.cancel()
        loadTask = Task { [weak self] in
            do {
                let page = try await loader.thread(username, request: first, loaded: 0)
                guard let self, token == self.generation else { return }
                self.loadedOnce = true
                self.messages = NativeMessage.merge([], page.items)
                self.seen = [first]
                self.next = page.next
                self.hasOlder = page.next != nil
                self.olderFailed = false
                self.phase = .loaded
                self.refreshing = false
                if scrollToLatest && !page.items.isEmpty { self.scrollToLatest += 1 }
                if self.visible { self.startPolling() }
            } catch {
                guard let self, token == self.generation, !(error is CancellationError) else { return }
                self.refreshing = false
                self.phase = self.messages.isEmpty ? .failed : .loaded
            }
        }
    }

    /// Loads the next older page and keeps the previously first message in view.
    func loadOlder() {
        guard phase == .loaded, !refreshing, !olderLoading, !olderFailed, let request = next else { return }
        olderLoading = true
        let token = generation, loader = loader, username = username, loaded = messages.count
        let anchor = messages.first?.id
        Task { [weak self] in
            do {
                let page = try await loader.thread(username, request: request, loaded: loaded)
                guard let self, token == self.generation else { return }
                let before = self.messages.count
                self.messages = NativeMessage.merge(self.messages, page.items)
                self.seen.insert(request)
                let repeated = page.next.map { self.seen.contains($0) } ?? false
                self.next = repeated || self.messages.count == before ? nil : page.next
                self.hasOlder = self.next != nil
                self.anchorAfterPrepend = anchor
                self.olderLoading = false
            } catch {
                guard let self, token == self.generation else { return }
                self.olderLoading = false
                self.olderFailed = !(error is CancellationError)
            }
        }
    }

    func retryOlder() {
        olderFailed = false
        loadOlder()
    }

    func anchorRestored() { anchorAfterPrepend = nil }

    /// Merges the newest messages without moving the reader's scroll position.
    func refreshNewer() {
        guard loadedOnce, phase == .loaded, !refreshing else { return }
        let token = generation, loader = loader, username = username
        Task { [weak self] in
            guard let values = try? await loader.newer(username), let self, token == self.generation else { return }
            let merged = NativeMessage.merge(self.messages, values)
            if merged != self.messages { self.messages = merged }
        }
    }

    private func startPolling() {
        guard pollTask == nil, visible else { return }
        let clock = clock
        pollTask = Task { [weak self] in
            while !Task.isCancelled {
                do { try await clock.sleep(seconds: Self.pollInterval) } catch { return }
                guard let self, !Task.isCancelled, self.visible else { return }
                self.refreshNewer()
            }
        }
    }

    private func stopPolling() {
        pollTask?.cancel()
        pollTask = nil
    }

    private func markRead() {
        let loader = loader
        Task {
            guard (try? await loader.readAll()) != nil else { return }
            try? await loader.refreshStatus()
        }
    }

    // MARK: Sending

    func acknowledgeUnknownOutcome() { outcomeUnknown = false }

    /// One send at a time; a failed send keeps the text and photo. An unclear outcome blocks
    /// resending until the user confirms, so a message is never sent twice by accident.
    func send() {
        guard canSend else { return }
        sending = true
        sendFailed = false
        let content = text.trimmingCharacters(in: .whitespacesAndNewlines)
        let adult = adult, photoKey = attachment.photoKey, loader = loader, username = username
        let token = generation
        Task { [weak self] in
            do {
                let message = try await loader.send(username, text: content, adult: adult, photoKey: photoKey)
                guard let self else { return }
                self.sending = false
                guard token == self.generation else { return }
                self.messages = NativeMessage.merge(self.messages, [message])
                self.text = ""
                self.adult = false
                self.attachment.markSent()
                self.scrollToLatest += 1
            } catch {
                guard let self else { return }
                self.sending = false
                self.sendFailed = true
                if let failure = error as? BridgeFailure {
                    self.outcomeUnknown = failure.category == "unknown" || failure.category == "server"
                }
            }
        }
    }

    /// Leaving deliberately drops the draft and any photo uploaded for it.
    func discardDraft() {
        text = ""
        adult = false
        attachment.discard()
    }
}
