import Foundation
import Observation
import PodkopShared

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
