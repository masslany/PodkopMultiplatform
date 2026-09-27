import Foundation
import Observation
import PodkopShared

@MainActor protocol CollectionLoading {
    func hitsFirstRequest() -> FeedRequest
    func hits(sort: String, archive: HitsArchive?, request: FeedRequest, loaded: Int) async throws
        -> ListPage<Resource>
    func rankFirstRequest() -> FeedRequest
    func rank(request: FeedRequest, loaded: Int) async throws -> ListPage<RankUser>
    func favouritesFirstRequest(isLoggedIn: Bool) -> FeedRequest
    func favourites(sort: String, type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<Resource>
    func observedFirstRequest() -> FeedRequest
    func observed(type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<ObservedItem>
}

@MainActor
final class SharedCollectionLoader: CollectionLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    private func bridge(_ request: FeedRequest) -> IOSPageRequest {
        IOSPageRequest(kind: request.kind, value: request.value)
    }

    func hitsFirstRequest() -> FeedRequest { FeedRequest(client.hits.firstRequest()) }

    func hits(sort: String, archive: HitsArchive?, request: FeedRequest, loaded: Int) async throws
        -> ListPage<Resource> {
        let page: IOSResourceListPage = try await adapter.call {
            self.client.hits.load(sort: sort, year: Int32(archive?.year ?? 0),
                                  month: Int32(archive?.month ?? 0), request: self.bridge(request),
                                  loaded: Int32(loaded), completion: $0)
        }
        return ListPage(page)
    }

    func rankFirstRequest() -> FeedRequest { FeedRequest(client.rank.firstRequest()) }

    func rank(request: FeedRequest, loaded: Int) async throws -> ListPage<RankUser> {
        let page: IOSRankPage = try await adapter.call {
            self.client.rank.load(request: self.bridge(request), loaded: Int32(loaded), completion: $0)
        }
        return ListPage(items: page.items.map {
            RankUser(username: $0.username, avatarURL: $0.avatarUrl, color: $0.color, gender: $0.gender,
                           memberSince: $0.memberSince, position: Int($0.position), trend: Int($0.trend),
                           actions: Int($0.actions), links: Int($0.links), entries: Int($0.entries),
                           followers: Int($0.followers))
        }, next: page.next.map(FeedRequest.init), total: page.total?.intValue)
    }

    func favouritesFirstRequest(isLoggedIn: Bool) -> FeedRequest {
        FeedRequest(client.favourites.firstRequest(isLoggedIn: isLoggedIn))
    }

    func favourites(sort: String, type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<Resource> {
        let page: IOSResourceListPage = try await adapter.call {
            self.client.favourites.load(sort: sort, type: type, request: self.bridge(request),
                                        loaded: Int32(loaded), completion: $0)
        }
        return ListPage(page)
    }

    func observedFirstRequest() -> FeedRequest { FeedRequest(client.observed.firstRequest()) }

    func observed(type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<ObservedItem> {
        let page: IOSObservedPage = try await adapter.call {
            self.client.observed.load(type: type, request: self.bridge(request),
                                      loaded: Int32(loaded), completion: $0)
        }
        return ListPage(items: page.items.map {
            ObservedItem(resource: Resource($0.resource),
                               newContentCount: $0.newContentCount?.intValue)
        }, next: page.next.map(FeedRequest.init), total: page.total?.intValue)
    }
}
