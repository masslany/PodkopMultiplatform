import Foundation
import Observation
import PodkopShared

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
