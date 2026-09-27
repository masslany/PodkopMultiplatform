import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureTagLoader: TagLoading {
    private var state = TagDetails(name: "technologia", description: "Nowinki technologiczne",
                                         followers: 1200, bannerURL: nil, observed: false,
                                         notificationsEnabled: false, blacklisted: false)
    func firstRequest(isLoggedIn: Bool) -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func details(_ tag: String) async throws -> TagDetails { state }
    func stream(_ tag: String, sort: String, type: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<Resource> {
        ListPage(items: type == "link" ? [ContentFixtures.link] : [ContentFixtures.link, ContentFixtures.entry, ContentFixtures.photoEntry],
                 next: nil, total: nil)
    }
    func setObserved(_ tag: String, _ enabled: Bool) async throws { state.observed = enabled }
    func setNotifications(_ tag: String, _ enabled: Bool) async throws { state.notificationsEnabled = enabled }
    func setBlacklisted(_ tag: String, _ enabled: Bool) async throws { state.blacklisted = enabled }
}
#endif
