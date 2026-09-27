import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureFeedLoader: FeedLoading {
    func policy(for query: FeedQuery) -> FeedPolicy {
        query.loggedIn && query.tab != .upcoming
            ? FeedPolicy(kind: "CursorInPage", initial: FeedRequest(kind: "initial"))
            : FeedPolicy(kind: "Numbered", initial: FeedRequest(kind: "number", value: "1"))
    }

    func nextRequest(for query: FeedQuery, next: String?, nextNumber: Int) -> FeedRequest? {
        query.loggedIn && query.tab != .upcoming
            ? next.map { FeedRequest(kind: "pageCursor", value: $0) }
            : FeedRequest(kind: "number", value: String(nextNumber))
    }

    func load(_ request: FeedRequest, query: FeedQuery) async throws -> FeedPage {
        let first = request.kind == "initial" || request.value == "1"
        return FeedPage(items: first ? [query.tab == .entries ? ContentFixtures.entry : ContentFixtures.link] : [],
                        next: first ? "2" : nil, total: 1)
    }

    func hits() async throws -> [Resource] { [ContentFixtures.link] }
}
#endif
