import Foundation
import Observation
import PodkopShared

@MainActor protocol FeedLoading {
    func policy(for query: FeedQuery) -> FeedPolicy
    func nextRequest(for query: FeedQuery, next: String?, nextNumber: Int) -> FeedRequest?
    func load(_ request: FeedRequest, query: FeedQuery) async throws -> FeedPage
    func hits() async throws -> [NativeResource]
}

@MainActor
final class SharedFeedLoader: FeedLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func policy(for query: FeedQuery) -> FeedPolicy {
        let value = query.tab == .entries
            ? client.entries.policy(isLoggedIn: query.loggedIn)
            : client.links.policy(isLoggedIn: query.loggedIn, isUpcoming: query.tab == .upcoming)
        return FeedPolicy(kind: value.kind, initial: FeedRequest(value.initial))
    }

    func nextRequest(for query: FeedQuery, next: String?, nextNumber: Int) -> FeedRequest? {
        let value = query.tab == .entries
            ? client.entries.nextRequest(isLoggedIn: query.loggedIn, next: next, nextNumber: Int32(nextNumber))
            : client.links.nextRequest(isLoggedIn: query.loggedIn, isUpcoming: query.tab == .upcoming,
                                       next: next, nextNumber: Int32(nextNumber))
        return value.map(FeedRequest.init)
    }

    func load(_ request: FeedRequest, query: FeedQuery) async throws -> FeedPage {
        let bridgeRequest = IOSPageRequest(kind: request.kind, value: request.value)
        let value: IOSResourcePage = try await adapter.call { completion in
            if query.tab == .entries {
                self.client.entries.load(request: bridgeRequest, sort: query.sort,
                                    hotHours: Int32(query.hotHours), completion: completion)
            } else {
                self.client.links.load(request: bridgeRequest, isUpcoming: query.tab == .upcoming,
                                  sort: query.sort, completion: completion)
            }
        }
        return FeedPage(items: value.items.map(NativeResource.init), next: value.next,
                        total: value.total?.intValue)
    }

    func hits() async throws -> [NativeResource] {
        let value: IOSResourcePage = try await adapter.call { self.client.links.hits(completion: $0) }
        return value.items.map(NativeResource.init)
    }
}
