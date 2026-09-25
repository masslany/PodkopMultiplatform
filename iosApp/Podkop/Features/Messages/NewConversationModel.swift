import Foundation
import Observation

@MainActor @Observable
final class NewConversationModel {
    enum Status: Equatable { case hidden, loading, loaded, failed }
    static let minimumQueryLength = 3
    var username = "" { didSet { if oldValue != username { schedule() } } }
    private(set) var suggestions: [UserSuggestion] = []
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
