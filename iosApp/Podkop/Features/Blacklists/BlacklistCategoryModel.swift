import Foundation
import Observation
import PodkopShared

@MainActor @Observable
final class BlacklistCategoryModel {
    enum SuggestionStatus: Equatable { case hidden, loading, loaded, failed }

    let category: BlacklistCategory
    let pager = ListPager<BlacklistEntry>(key: \.id)
    var input = "" { didSet { if oldValue != input { scheduleSuggestions() } } }
    private(set) var busy = false
    private(set) var failed = false
    private(set) var suggestions: [BlacklistSuggestion] = []
    private(set) var suggestionStatus: SuggestionStatus = .hidden
    private let loader: BlacklistsLoading
    private let suggester: SearchSuggesting
    private let debounce: Duration
    private var suggestionTask: Task<Void, Never>?
    private var suggestionRevision = 0

    var canSubmit: Bool { !busy && !loader.normalize(category, input).isEmpty }
    var total: Int? { pager.phase == .loaded ? pager.total ?? pager.items.count : nil }

    init(category: BlacklistCategory, loader: BlacklistsLoading, suggester: SearchSuggesting,
         debounce: Duration = .milliseconds(300)) {
        self.category = category
        self.loader = loader
        self.suggester = suggester
        self.debounce = debounce
    }

    func start() {
        guard pager.phase == .idle else { return }
        let loader = loader, category = category
        pager.load(first: loader.firstRequest()) { try await loader.load(category, request: $0, loaded: $1) }
    }

    func refresh() async { await pager.refresh() }

    func stop() {
        pager.stop()
        suggestionTask?.cancel()
    }

    func submit(_ value: String? = nil) {
        let normalized = loader.normalize(category, value ?? input)
        guard !normalized.isEmpty, !busy else { return }
        busy = true
        failed = false
        let loader = loader, category = category
        Task { [weak self] in
            do {
                try await loader.add(category, normalized)
                guard let self else { return }
                self.input = ""
                self.suggestionTask?.cancel()
                self.suggestions = []
                self.suggestionStatus = .hidden
                self.busy = false
                await self.pager.refresh()
            } catch {
                guard let self else { return }
                self.busy = false
                self.failed = !(error is CancellationError)
            }
        }
    }

    /// Confirmation happens in the view; this removes only after the server confirms.
    func remove(_ entry: BlacklistEntry) {
        guard !busy else { return }
        busy = true
        failed = false
        let loader = loader
        Task { [weak self] in
            do {
                try await loader.remove(entry)
                guard let self else { return }
                self.pager.update { $0.id == entry.id ? nil : $0 }
                self.busy = false
            } catch {
                guard let self else { return }
                self.busy = false
                self.failed = !(error is CancellationError)
            }
        }
    }

    func dismissFailure() { failed = false }

    func retrySuggestions() { scheduleSuggestions(immediately: true) }

    private func scheduleSuggestions(immediately: Bool = false) {
        suggestionRevision += 1
        suggestionTask?.cancel()
        let query = loader.normalize(category, input)
        guard category != .domains, query.count >= 3 else {
            suggestions = []
            suggestionStatus = .hidden
            return
        }
        let token = suggestionRevision, delay = debounce, suggester = suggester, category = category
        suggestionTask = Task { [weak self] in
            if !immediately { try? await Task.sleep(for: delay) }
            guard let self, !Task.isCancelled, token == self.suggestionRevision else { return }
            self.suggestionStatus = .loading
            do {
                let values: [BlacklistSuggestion] = category == .users
                    ? try await suggester.users(query).map(BlacklistSuggestion.user)
                    : try await suggester.tags(query).map(BlacklistSuggestion.tag)
                guard token == self.suggestionRevision, !Task.isCancelled else { return }
                // Values already on the list are not offered again.
                let listed = Set(self.pager.items.map(\.value))
                self.suggestions = values.filter { !listed.contains(self.loader.normalize(category, $0.value)) }
                self.suggestionStatus = .loaded
            } catch {
                guard token == self.suggestionRevision, !(error is CancellationError) else { return }
                self.suggestionStatus = .failed
            }
        }
    }
}
