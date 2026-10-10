import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureNotificationsLoader: NotificationsLoading {
    private var read = Set<String>()
    func firstRequest(_ group: NotificationGroupKind) -> FeedRequest { FeedRequest(kind: "initial") }
    func load(_ group: NotificationGroupKind, request: FeedRequest, loaded: Int) async throws
        -> ListPage<AppNotification> {
        let now = Date()
        func item(_ id: String, _ target: NotificationTargetValue, message: String? = nil, tag: String? = nil,
                  groupID: String? = nil, count: Int = 1, entry: Int? = nil, link: Int? = nil,
                  actor: String? = "Ewa-Żółw") -> AppNotification {
            AppNotification(id: id, group: group, isRead: read.contains(id), groupID: groupID, groupCount: count,
                               showAsGroup: count > 1, groupUpdatedAt: count > 1 ? now : nil,
                               createdAt: now, actor: actor, actorColor: "green", actorGender: "female",
                               message: message, issueTitle: nil, badgeName: nil, tagName: tag, linkID: link,
                               linkTitle: link.map { _ in "Przykładowy link" }, entryID: entry,
                               entryContent: entry.map { _ in "Treść wpisu" }, target: target)
        }
        let items: [AppNotification] = switch group {
        case .entries: [item("e1", .entry(102), message: "wspomniał Cię we wpisie", entry: 102)]
        case .pm: [item("Ewa-Żółw", .conversation("Ewa-Żółw"), message: "Cześć!")]
        // Like the API's `show_grouped=1`: one row per group, its members listed by `loadGroup`.
        case .tags: [item("t1", .tag("technologia"), tag: "technologia", groupID: "g1", count: 3, entry: 1, actor: nil)]
        case .observedDiscussions: [item("o1", .link(101), link: 101)]
        }
        return ListPage(items: items, next: nil, total: items.count)
    }
    func firstGroupRequest() -> FeedRequest { FeedRequest(kind: "number", value: "1") }
    /// Group "g1" holds three new entries, two per page, so the second page needs "Pokaż więcej".
    func loadGroup(_ group: NotificationGroupKind, groupID: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<AppNotification> {
        let page = Int(request.value ?? "") ?? 1
        let ids = page == 1 ? ["g1-1", "g1-2"] : ["g1-3"]
        let members = ids.map { id in
            AppNotification(id: id, group: group, isRead: read.contains(id), groupID: groupID, groupCount: 3,
                            createdAt: Date(), actor: "Ewa-Żółw", actorColor: "green", actorGender: "female",
                            message: nil, issueTitle: nil, badgeName: nil, tagName: "technologia", linkID: nil,
                            linkTitle: nil, entryID: 102, entryContent: "Wpis z tagiem \(id)", target: .entry(102))
        }
        return ListPage(items: members, next: page == 1 ? FeedRequest(kind: "number", value: "2") : nil, total: 3)
    }
    func refreshStatus() async throws {}
    func markAsRead(_ group: NotificationGroupKind, id: String) async throws { read.insert(id) }
    func markAllAsRead(_ group: NotificationGroupKind) async throws {}
}
#endif
