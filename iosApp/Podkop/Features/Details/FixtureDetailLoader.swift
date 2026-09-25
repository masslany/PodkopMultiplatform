import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureDetailLoader: DetailLoading {
    func resource(kind: ResourceKind, id: Int) async throws -> Resource {
        kind == .link ? ContentFixtures.link : ContentFixtures.entry
    }
    func comments(kind: ResourceKind, id: Int, page: Int, sort: String) async throws -> FeedPage {
        FeedPage(items: page == 1 ? [kind == .link ? ContentFixtures.linkComment : ContentFixtures.entryComment] : [],
                 next: nil, total: 1)
    }
    func replies(linkID: Int, commentID: Int, page: Int) async throws -> FeedPage {
        FeedPage(items: [], next: nil, total: 0)
    }
    func related(linkID: Int) async throws -> [Resource] { [] }
}
#endif
