import Foundation
import Observation
import PodkopShared

struct NativeTagDetails: Equatable {
    var name: String
    var description: String
    var followers: Int
    var bannerURL: String?
    var observed: Bool
    var notificationsEnabled: Bool
    var blacklisted: Bool
}

@MainActor protocol TagLoading {
    func firstRequest(isLoggedIn: Bool) -> FeedRequest
    func details(_ tag: String) async throws -> NativeTagDetails
    func stream(_ tag: String, sort: String, type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeResource>
    func setObserved(_ tag: String, _ enabled: Bool) async throws
    func setNotifications(_ tag: String, _ enabled: Bool) async throws
    func setBlacklisted(_ tag: String, _ enabled: Bool) async throws
}

@MainActor
final class SharedTagLoader: TagLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func firstRequest(isLoggedIn: Bool) -> FeedRequest {
        FeedRequest(client.tag.firstRequest(isLoggedIn: isLoggedIn))
    }

    func details(_ tag: String) async throws -> NativeTagDetails {
        let value: IOSTagDetails = try await adapter.call { self.client.tag.details(tag: tag, completion: $0) }
        return NativeTagDetails(name: value.name, description: value.description_,
                                followers: Int(value.followers), bannerURL: value.bannerUrl,
                                observed: value.observed, notificationsEnabled: value.notificationsEnabled,
                                blacklisted: value.blacklisted)
    }

    func stream(_ tag: String, sort: String, type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeResource> {
        let page: IOSResourceListPage = try await adapter.call {
            self.client.tag.stream(tag: tag, sort: sort, type: type,
                                   request: IOSPageRequest(kind: request.kind, value: request.value),
                                   loaded: Int32(loaded), completion: $0)
        }
        return ListPage(page)
    }

    func setObserved(_ tag: String, _ enabled: Bool) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.tag.setObserved(tag: tag, enabled: enabled, completion: $0)
        }
    }

    func setNotifications(_ tag: String, _ enabled: Bool) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.tag.setNotifications(tag: tag, enabled: enabled, completion: $0)
        }
    }

    func setBlacklisted(_ tag: String, _ enabled: Bool) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.tag.setBlacklisted(tag: tag, enabled: enabled, completion: $0)
        }
    }
}

@MainActor @Observable
final class TagModel {
    enum Sort: String, CaseIterable { case all, best }
    enum Kind: String, CaseIterable { case all, link, entry }
    enum Action: Hashable { case observe, notifications, blacklist }

    let tag: String
    private(set) var details: NativeTagDetails?
    private(set) var detailsFailed = false
    private(set) var sort: Sort = .all
    private(set) var kind: Kind = .all
    private(set) var isLoggedIn: Bool
    private(set) var pending = Set<Action>()
    private(set) var actionFailed = false
    var gallery = false
    let pager = ListPager<NativeResource>()

    private let loader: TagLoading
    private let updates: ResourceUpdates?
    private var detailsTask: Task<Void, Never>?
    private var generation = 0
    private var appliedUpdateRevision = 0

    /// Entries with a photo, in stream order, as Android's gallery shows them.
    var galleryItems: [NativeResource] {
        pager.items.filter { $0.kind == .entry && $0.photo != nil && $0.deletion == nil }
    }

    init(tag: String, isLoggedIn: Bool, loader: TagLoading, updates: ResourceUpdates? = nil) {
        self.tag = tag
        self.isLoggedIn = isLoggedIn
        self.loader = loader
        self.updates = updates
    }

    func start() {
        guard pager.phase == .idle else { return }
        if details == nil { loadDetails() }
        reloadStream()
    }

    func select(sort: Sort) {
        guard sort != self.sort else { return }
        self.sort = sort
        reloadStream()
    }

    func select(kind: Kind) {
        guard kind != self.kind else { return }
        self.kind = kind
        reloadStream()
    }

    func refresh() async {
        loadDetails()
        await pager.refresh()
    }

    /// A new session may change observation state and the stream's pagination policy.
    func setSession(_ loggedIn: Bool) {
        generation += 1
        isLoggedIn = loggedIn
        pending.removeAll()
        details = nil
        loadDetails()
        reloadStream()
    }

    func stop() {
        pager.stop()
        detailsTask?.cancel()
    }

    func toggle(_ action: Action) {
        guard isLoggedIn, let current = details, !pending.contains(action) else { return }
        if action == .notifications && !current.observed { return }
        pending.insert(action)
        actionFailed = false
        let token = generation, loader = loader, tag = tag
        Task { [weak self] in
            do {
                switch action {
                case .observe: try await loader.setObserved(tag, !current.observed)
                case .notifications: try await loader.setNotifications(tag, !current.notificationsEnabled)
                case .blacklist: try await loader.setBlacklisted(tag, !current.blacklisted)
                }
                guard let self, token == self.generation else { return }
                self.pending.remove(action)
                self.applyConfirmed(action)
            } catch {
                guard let self, token == self.generation else { return }
                self.pending.remove(action)
                if !(error is CancellationError) { self.actionFailed = true }
            }
        }
    }

    func dismissActionFailure() { actionFailed = false }

    /// State changes only after the server confirms, as on Android; failures leave it untouched.
    private func applyConfirmed(_ action: Action) {
        guard var value = details else { return }
        switch action {
        case .observe:
            value.observed.toggle()
            if !value.observed { value.notificationsEnabled = false }
        case .notifications:
            value.notificationsEnabled.toggle()
        case .blacklist:
            value.blacklisted.toggle()
        }
        details = value
    }

    private func loadDetails() {
        detailsTask?.cancel()
        detailsFailed = false
        let token = generation, loader = loader, tag = tag
        detailsTask = Task { [weak self] in
            do {
                let value = try await loader.details(tag)
                guard let self, token == self.generation, !Task.isCancelled else { return }
                self.details = value
            } catch is CancellationError {
                return
            } catch {
                guard let self, token == self.generation else { return }
                self.detailsFailed = true
            }
        }
    }

    private func reloadStream() {
        let sort = sort.rawValue, kind = kind.rawValue, loader = loader, updates = updates, tag = tag
        pager.load(first: loader.firstRequest(isLoggedIn: isLoggedIn)) { request, loaded in
            let page = try await loader.stream(tag, sort: sort, type: kind, request: request, loaded: loaded)
            return reconciled(page, updates)
        }
    }

    func reconcile(_ updates: ResourceUpdates) {
        let reload = updates.needsReload(pager.items, since: appliedUpdateRevision)
        appliedUpdateRevision = updates.revision
        pager.update(updates.reconcile)
        if reload { Task { await pager.refresh() } }
    }
}

#if DEBUG
@MainActor
final class FixtureTagLoader: TagLoading {
    private var state = NativeTagDetails(name: "technologia", description: "Nowinki technologiczne",
                                         followers: 1200, bannerURL: nil, observed: false,
                                         notificationsEnabled: false, blacklisted: false)
    func firstRequest(isLoggedIn: Bool) -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func details(_ tag: String) async throws -> NativeTagDetails { state }
    func stream(_ tag: String, sort: String, type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeResource> {
        ListPage(items: type == "link" ? [ContentFixtures.link] : [ContentFixtures.link, ContentFixtures.entry],
                 next: nil, total: nil)
    }
    func setObserved(_ tag: String, _ enabled: Bool) async throws { state.observed = enabled }
    func setNotifications(_ tag: String, _ enabled: Bool) async throws { state.notificationsEnabled = enabled }
    func setBlacklisted(_ tag: String, _ enabled: Bool) async throws { state.blacklisted = enabled }
}
#endif
