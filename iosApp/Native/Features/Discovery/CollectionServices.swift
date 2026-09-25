import Foundation
import PodkopShared

struct HitsArchive: Hashable {
    static let startYear = 2007
    static let startMonth = 12
    let year: Int
    let month: Int

    static func isAvailable(year: Int, month: Int, now: Date = Date(),
                            calendar: Calendar = .current) -> Bool {
        let current = calendar.dateComponents([.year, .month], from: now)
        guard let maxYear = current.year, let maxMonth = current.month,
              (1...12).contains(month), (startYear...maxYear).contains(year) else { return false }
        if year == startYear && month < startMonth { return false }
        if year == maxYear && month > maxMonth { return false }
        return true
    }
}

struct NativeRankUser: Identifiable, Equatable {
    let username: String
    let color: String
    let gender: String
    let memberSince: String?
    let position: Int
    let trend: Int
    let actions: Int
    let links: Int
    let entries: Int
    let followers: Int
    var id: String { username }
}

struct NativeObservedItem: Identifiable {
    let resource: NativeResource
    let newContentCount: Int?
    var id: String { "\(resource.id):\(resource.parentID ?? 0)" }
}

@MainActor protocol CollectionLoading {
    func hitsFirstRequest() -> FeedRequest
    func hits(sort: String, archive: HitsArchive?, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeResource>
    func rankFirstRequest() -> FeedRequest
    func rank(request: FeedRequest, loaded: Int) async throws -> ListPage<NativeRankUser>
    func favouritesFirstRequest(isLoggedIn: Bool) -> FeedRequest
    func favourites(sort: String, type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeResource>
    func observedFirstRequest() -> FeedRequest
    func observed(type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeObservedItem>
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
        -> ListPage<NativeResource> {
        let page: IOSResourceListPage = try await adapter.call {
            self.client.hits.load(sort: sort, year: Int32(archive?.year ?? 0),
                                  month: Int32(archive?.month ?? 0), request: self.bridge(request),
                                  loaded: Int32(loaded), completion: $0)
        }
        return ListPage(page)
    }

    func rankFirstRequest() -> FeedRequest { FeedRequest(client.rank.firstRequest()) }

    func rank(request: FeedRequest, loaded: Int) async throws -> ListPage<NativeRankUser> {
        let page: IOSRankPage = try await adapter.call {
            self.client.rank.load(request: self.bridge(request), loaded: Int32(loaded), completion: $0)
        }
        return ListPage(items: page.items.map {
            NativeRankUser(username: $0.username, color: $0.color, gender: $0.gender,
                           memberSince: $0.memberSince, position: Int($0.position), trend: Int($0.trend),
                           actions: Int($0.actions), links: Int($0.links), entries: Int($0.entries),
                           followers: Int($0.followers))
        }, next: page.next.map(FeedRequest.init), total: page.total?.intValue)
    }

    func favouritesFirstRequest(isLoggedIn: Bool) -> FeedRequest {
        FeedRequest(client.favourites.firstRequest(isLoggedIn: isLoggedIn))
    }

    func favourites(sort: String, type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeResource> {
        let page: IOSResourceListPage = try await adapter.call {
            self.client.favourites.load(sort: sort, type: type, request: self.bridge(request),
                                        loaded: Int32(loaded), completion: $0)
        }
        return ListPage(page)
    }

    func observedFirstRequest() -> FeedRequest { FeedRequest(client.observed.firstRequest()) }

    func observed(type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeObservedItem> {
        let page: IOSObservedPage = try await adapter.call {
            self.client.observed.load(type: type, request: self.bridge(request),
                                      loaded: Int32(loaded), completion: $0)
        }
        return ListPage(items: page.items.map {
            NativeObservedItem(resource: NativeResource($0.resource),
                               newContentCount: $0.newContentCount?.intValue)
        }, next: page.next.map(FeedRequest.init), total: page.total?.intValue)
    }
}

#if DEBUG
@MainActor
final class FixtureCollectionLoader: CollectionLoading {
    private let first = FeedRequest(kind: "number", value: "1")
    func hitsFirstRequest() -> FeedRequest { first }
    func hits(sort: String, archive: HitsArchive?, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeResource> { ListPage(items: [ContentFixtures.link], next: nil, total: 1) }
    func rankFirstRequest() -> FeedRequest { first }
    func rank(request: FeedRequest, loaded: Int) async throws -> ListPage<NativeRankUser> {
        ListPage(items: [NativeRankUser(username: "Ewa-Żółw", color: "orange", gender: "female",
                                        memberSince: "2012-03-01T10:00", position: 1, trend: 2,
                                        actions: 120, links: 10, entries: 90, followers: 400)],
                 next: nil, total: 1)
    }
    func favouritesFirstRequest(isLoggedIn: Bool) -> FeedRequest { FeedRequest(kind: "initial") }
    func favourites(sort: String, type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeResource> { ListPage(items: [ContentFixtures.entry], next: nil, total: 1) }
    func observedFirstRequest() -> FeedRequest { FeedRequest(kind: "initial") }
    func observed(type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeObservedItem> {
        ListPage(items: [NativeObservedItem(resource: ContentFixtures.link, newContentCount: 3)],
                 next: nil, total: 1)
    }
}
#endif
