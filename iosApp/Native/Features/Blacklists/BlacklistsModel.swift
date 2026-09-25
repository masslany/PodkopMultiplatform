import Foundation
import Observation
import PodkopShared

enum BlacklistCategory: String, CaseIterable {
    case users, tags, domains
}

struct BlacklistEntry: Identifiable, Equatable {
    let category: BlacklistCategory
    /// Normalized value used for removal and routing.
    let value: String
    let color: String?
    let gender: String?
    var id: String { "\(category.rawValue):\(value)" }
    var label: String { category == .tags ? "#\(value)" : value }
}

@MainActor protocol BlacklistsLoading {
    func firstRequest() -> FeedRequest
    func normalize(_ category: BlacklistCategory, _ value: String) -> String
    func load(_ category: BlacklistCategory, request: FeedRequest, loaded: Int) async throws -> ListPage<BlacklistEntry>
    func add(_ category: BlacklistCategory, _ value: String) async throws
    func remove(_ entry: BlacklistEntry) async throws
}

@MainActor
final class SharedBlacklistsLoader: BlacklistsLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func firstRequest() -> FeedRequest { FeedRequest(client.blacklists.firstRequest()) }

    func normalize(_ category: BlacklistCategory, _ value: String) -> String {
        client.blacklists.normalize(category: category.rawValue, value: value)
    }

    func load(_ category: BlacklistCategory, request: FeedRequest, loaded: Int) async throws
        -> ListPage<BlacklistEntry> {
        let page: IOSBlacklistPage = try await adapter.call {
            self.client.blacklists.load(category: category.rawValue,
                                        request: IOSPageRequest(kind: request.kind, value: request.value),
                                        loaded: Int32(loaded), completion: $0)
        }
        return ListPage(items: page.items.map {
            BlacklistEntry(category: category, value: $0.value, color: $0.color, gender: $0.gender)
        }, next: page.next.map(FeedRequest.init), total: page.total?.intValue)
    }

    func add(_ category: BlacklistCategory, _ value: String) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.blacklists.add(category: category.rawValue, value: value, completion: $0)
        }
    }

    func remove(_ entry: BlacklistEntry) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.blacklists.remove(category: entry.category.rawValue, value: entry.value, completion: $0)
        }
    }
}

enum BlacklistSuggestion: Identifiable, Equatable {
    case user(NativeUserSuggestion)
    case tag(NativeTagSuggestion)
    var id: String {
        switch self {
        case .user(let value): "user:\(value.username)"
        case .tag(let value): "tag:\(value.name)"
        }
    }
    var value: String {
        switch self {
        case .user(let value): value.username
        case .tag(let value): value.name
        }
    }
}

/// One category's list, add form, and suggestions. Categories load independently, as on Android.
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

@MainActor @Observable
final class BlacklistsModel {
    var selected: BlacklistCategory = .users
    let categories: [BlacklistCategory: BlacklistCategoryModel]

    init(loader: BlacklistsLoading, suggester: SearchSuggesting) {
        categories = Dictionary(uniqueKeysWithValues: BlacklistCategory.allCases.map {
            ($0, BlacklistCategoryModel(category: $0, loader: loader, suggester: suggester))
        })
    }

    var current: BlacklistCategoryModel { categories[selected]! }

    func start() { categories.values.forEach { $0.start() } }

    func refresh() async {
        await withTaskGroup(of: Void.self) { group in
            for model in categories.values { group.addTask { await model.refresh() } }
        }
    }

    func stop() { categories.values.forEach { $0.stop() } }
}

#if DEBUG
@MainActor
final class FixtureBlacklistsLoader: BlacklistsLoading {
    private var values: [BlacklistCategory: [String]] = [.users: ["spamer"], .tags: ["polityka"], .domains: ["example.com"]]
    func firstRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func normalize(_ category: BlacklistCategory, _ value: String) -> String {
        let trimmed = value.trimmingCharacters(in: .whitespaces)
        switch category {
        case .users: return trimmed.hasPrefix("@") ? String(trimmed.dropFirst()) : trimmed
        case .tags: return (trimmed.hasPrefix("#") ? String(trimmed.dropFirst()) : trimmed).lowercased()
        case .domains: return trimmed.lowercased()
        }
    }
    func load(_ category: BlacklistCategory, request: FeedRequest, loaded: Int) async throws
        -> ListPage<BlacklistEntry> {
        let items = values[category, default: []].map {
            BlacklistEntry(category: category, value: $0, color: category == .users ? "green" : nil, gender: nil)
        }
        return ListPage(items: items, next: nil, total: items.count)
    }
    func add(_ category: BlacklistCategory, _ value: String) async throws { values[category, default: []].append(value) }
    func remove(_ entry: BlacklistEntry) async throws { values[entry.category]?.removeAll { $0 == entry.value } }
}
#endif
