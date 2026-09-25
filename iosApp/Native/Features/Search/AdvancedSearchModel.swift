import Foundation
import Observation
import PodkopShared

struct AdvancedSearchForm: Equatable {
    enum Sort: String, CaseIterable { case score, popular, comments, newest }
    enum DatePreset: String, CaseIterable {
        case anyTime, last24Hours, last3Days, last7Days, last30Days, lastYear, custom
    }
    static let voteOptions: [Int?] = [nil, 50, 100, 500, 1000]

    var query = ""
    var sort: Sort = .score
    var minimumVotes: Int?
    var datePreset: DatePreset = .anyTime
    var customDateFrom = ""
    var customDateTo = ""
    var tags = ""
    var users = ""
    var domains = ""
    var category = ""
}

enum AdvancedSearchValidation: String, Error, Equatable {
    case queryRequired, invalidCustomDateFormat, invalidCustomDateRange, invalid
}

/// Opaque request built and validated by the shared layer.
struct AdvancedSearchRequest {
    let value: IOSSearchQuery?
}

@MainActor protocol AdvancedSearching {
    func build(_ form: AdvancedSearchForm) -> Result<AdvancedSearchRequest, AdvancedSearchValidation>
    func firstRequest() -> FeedRequest
    func load(_ query: AdvancedSearchRequest, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeResource>
}

@MainActor
final class SharedAdvancedSearching: AdvancedSearching {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func build(_ form: AdvancedSearchForm) -> Result<AdvancedSearchRequest, AdvancedSearchValidation> {
        let result = client.search.build(
            query: form.query, sort: form.sort.rawValue, minimumVotes: Int32(form.minimumVotes ?? 0),
            datePreset: form.datePreset.rawValue, customDateFrom: form.customDateFrom,
            customDateTo: form.customDateTo, tags: form.tags, users: form.users,
            domains: form.domains, category: form.category
        )
        if let query = result.query { return .success(AdvancedSearchRequest(value: query)) }
        return .failure(result.error.flatMap(AdvancedSearchValidation.init(rawValue:)) ?? .invalid)
    }

    func firstRequest() -> FeedRequest { FeedRequest(client.search.firstStreamRequest()) }

    func load(_ query: AdvancedSearchRequest, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeResource> {
        guard let value = query.value else { throw BridgeFailure(category: "validation", code: nil) }
        let bridgeRequest = IOSPageRequest(kind: request.kind, value: request.value)
        let page: IOSResourceListPage = try await adapter.call {
            self.client.search.stream(query: value, request: bridgeRequest, loaded: Int32(loaded),
                                      completion: $0)
        }
        return ListPage(page)
    }
}

@MainActor @Observable
final class AdvancedSearchModel {
    var form: AdvancedSearchForm { didSet { if oldValue != form { validation = nil } } }
    private(set) var validation: AdvancedSearchValidation?
    private(set) var hasSearched = false
    let results = ListPager<NativeResource>()
    private let service: AdvancedSearching
    private let updates: ResourceUpdates?
    private var appliedUpdateRevision = 0

    var canSearch: Bool {
        !form.query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
            && results.phase != .loading && !results.isRefreshing
    }

    init(initialQuery: String, service: AdvancedSearching, updates: ResourceUpdates? = nil) {
        form = AdvancedSearchForm(query: initialQuery)
        self.service = service
        self.updates = updates
    }

    /// Builds the request once so a relative date window stays fixed while paging.
    func search() {
        switch service.build(form) {
        case .failure(let error):
            validation = error
        case .success(let request):
            validation = nil
            hasSearched = true
            let service = self.service
            results.load(first: service.firstRequest()) { [updates] page, loaded in
                let value = try await service.load(request, request: page, loaded: loaded)
                guard let updates else { return value }
                return ListPage(items: value.items.compactMap(updates.reconcile),
                                next: value.next, total: value.total)
            }
        }
    }

    func refresh() async { await results.refresh() }

    func resume() { results.resume() }

    func stop() { results.stop() }

    func reconcile(_ updates: ResourceUpdates) {
        let reload = updates.needsReload(results.items, since: appliedUpdateRevision)
        appliedUpdateRevision = updates.revision
        results.update(updates.reconcile)
        if reload { Task { await results.refresh() } }
    }
}

#if DEBUG
@MainActor
final class FixtureAdvancedSearching: AdvancedSearching {
    func build(_ form: AdvancedSearchForm) -> Result<AdvancedSearchRequest, AdvancedSearchValidation> {
        form.query.trimmingCharacters(in: .whitespaces).isEmpty
            ? .failure(.queryRequired) : .success(AdvancedSearchRequest(value: nil))
    }
    func firstRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func load(_ query: AdvancedSearchRequest, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeResource> {
        ListPage(items: [ContentFixtures.link], next: nil, total: 1)
    }
}
#endif
