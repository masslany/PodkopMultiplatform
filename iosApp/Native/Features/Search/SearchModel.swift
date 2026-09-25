import Foundation
import Observation
import PodkopShared

@MainActor @Observable
final class NativeSearchModel {
    enum Status { case idle, loading, loaded, failed }
    var query = "" { didSet { if oldValue != query { schedule() } } }
    private(set) var tags: [NativeTagSuggestion] = []
    private(set) var users: [NativeUserSuggestion] = []
    private(set) var tagsStatus: Status = .idle
    private(set) var usersStatus: Status = .idle
    private(set) var isLoggedIn: Bool
    let minimumQueryLength = 3
    var normalizedQuery: String { query.trimmingCharacters(in: .whitespacesAndNewlines) }
    private let service: SearchSuggesting
    private let debounceDelay: Duration
    private var debounce: Task<Void, Never>?
    private var tagsTask: Task<Void, Never>?
    private var usersTask: Task<Void, Never>?
    private var revision = 0

    init(service: SearchSuggesting, isLoggedIn: Bool, debounce: Duration = .milliseconds(300)) {
        self.service = service
        self.isLoggedIn = isLoggedIn
        debounceDelay = debounce
    }

    func setSession(_ value: Bool) {
        guard value != isLoggedIn else { return }
        isLoggedIn = value
        usersTask?.cancel()
        users = []
        usersStatus = .idle
        schedule()
    }

    private func schedule() {
        revision += 1
        let token = revision
        debounce?.cancel()
        tagsTask?.cancel()
        usersTask?.cancel()
        tags = []
        users = []
        tagsStatus = .idle
        usersStatus = .idle
        let normalized = normalizedQuery
        guard normalized.count >= minimumQueryLength else { return }
        let delay = debounceDelay
        debounce = Task { [weak self] in
            try? await Task.sleep(for: delay)
            guard let self, !Task.isCancelled, token == revision else { return }
            loadTags(normalized, token: token)
            if isLoggedIn { loadUsers(normalized, token: token) }
        }
    }

    func retryTags() {
        let normalized = normalizedQuery
        guard normalized.count >= minimumQueryLength else { return }
        loadTags(normalized, token: revision)
    }

    func retryUsers() {
        let normalized = normalizedQuery
        guard isLoggedIn, normalized.count >= minimumQueryLength else { return }
        loadUsers(normalized, token: revision)
    }

    private func loadTags(_ query: String, token: Int) {
        tagsTask?.cancel()
        tagsStatus = .loading
        tagsTask = Task { [weak self] in
            guard let self else { return }
            do {
                let result = try await service.tags(query)
                guard token == revision, !Task.isCancelled else { return }
                tags = result
                tagsStatus = .loaded
            } catch is CancellationError {
                return
            } catch {
                if token == revision { tagsStatus = .failed }
            }
        }
    }

    private func loadUsers(_ query: String, token: Int) {
        usersTask?.cancel()
        usersStatus = .loading
        usersTask = Task { [weak self] in
            guard let self else { return }
            do {
                let result = try await service.users(query)
                guard token == revision, !Task.isCancelled, isLoggedIn else { return }
                users = result
                usersStatus = .loaded
            } catch is CancellationError {
                return
            } catch {
                if token == revision, isLoggedIn { usersStatus = .failed }
            }
        }
    }

    /// Restarts suggestions interrupted by `stop()` when the screen becomes visible again.
    func resume() {
        let interrupted = tagsStatus == .loading || usersStatus == .loading
        let neverLoaded = tagsStatus == .idle && normalizedQuery.count >= minimumQueryLength
        if interrupted || neverLoaded { schedule() }
    }

    func stop() {
        revision += 1
        debounce?.cancel()
        tagsTask?.cancel()
        usersTask?.cancel()
    }
}
