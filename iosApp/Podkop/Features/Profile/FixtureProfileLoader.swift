import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureProfileLoader: ProfileLoading {
    func ownUsername() async throws -> String { "Ja" }
    func profile(_ username: String) async throws -> Profile {
        Profile(username: username, rankPosition: 12, color: "orange", gender: "female",
                      memberSince: "2012-03-01T10:00", actions: 120, links: 10, entries: 90, followers: 400,
                      following: 7, observed: false, blacklisted: false, isLoggedIn: true,
                      isOwnProfile: username == "Ja", canManageObservation: username != "Ja",
                      canBlacklist: username != "Ja", canSendPrivateMessage: username != "Ja")
    }
    func badges(_ username: String) async throws -> [Badge] {
        [Badge(label: "Weteran", slug: "veteran", description: "10 lat na Wykopie", colorHex: "#ff9900",
                     level: 2, progress: 50, achievedAt: "2022-01-01T10:00")]
    }
    func note(_ username: String) async throws -> String { "" }
    func saveNote(_ username: String, _ content: String) async throws {}
    func setObserved(_ username: String, _ enabled: Bool) async throws {}
    func setBlacklisted(_ username: String, _ enabled: Bool) async throws {}
    func firstSectionRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    func section(_ username: String, _ section: ProfileSection, request: FeedRequest, loaded: Int) async throws
        -> ListPage<ProfileRow> {
        switch section {
        case .followers, .followingUsers:
            ListPage(items: [.user(username: "Ewa-Żółw", color: "green", gender: "female", online: true, verified: false)],
                     next: nil, total: 1)
        case .followingTags:
            ListPage(items: [.tag(name: "technologia", pinned: true)], next: nil, total: 1)
        default:
            ListPage(items: [.resource(ContentFixtures.entry)], next: nil, total: 1)
        }
    }
}
#endif
