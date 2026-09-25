import Foundation
import Observation
import PodkopShared

#if DEBUG
@MainActor
final class FixtureNotificationsLoader: NotificationsLoading {
    private var read = Set<String>()
    func firstRequest(_ group: NotificationGroupKind) -> FeedRequest { FeedRequest(kind: "initial") }
    func load(_ group: NotificationGroupKind, request: FeedRequest, loaded: Int) async throws
        -> ListPage<NativeNotification> {
        let now = Date()
        func item(_ id: String, _ target: NotificationTargetValue, message: String? = nil, tag: String? = nil,
                  groupID: String? = nil, count: Int = 1, entry: Int? = nil, link: Int? = nil,
                  actor: String? = "Ewa-Żółw") -> NativeNotification {
            NativeNotification(id: id, group: group, isRead: read.contains(id), groupID: groupID, groupCount: count,
                               createdAt: now, actor: actor, actorColor: "green", actorGender: "female",
                               message: message, issueTitle: nil, badgeName: nil, tagName: tag, linkID: link,
                               linkTitle: link.map { _ in "Przykładowy link" }, entryID: entry,
                               entryContent: entry.map { _ in "Treść wpisu" }, target: target)
        }
        let items: [NativeNotification] = switch group {
        case .entries: [item("e1", .entry(102), message: "wspomniał Cię we wpisie", entry: 102)]
        case .pm: [item("Ewa-Żółw", .conversation("Ewa-Żółw"), message: "Cześć!")]
        case .tags: [item("t1", .tag("technologia"), tag: "technologia", groupID: "g1", count: 2, entry: 1, actor: nil),
                     item("t2", .tag("technologia"), tag: "technologia", groupID: "g1", count: 2, entry: 2, actor: nil)]
        case .observedDiscussions: [item("o1", .link(101), link: 101)]
        }
        return ListPage(items: items, next: nil, total: items.count)
    }
    func refreshStatus() async throws {}
    func markAsRead(_ group: NotificationGroupKind, id: String) async throws { read.insert(id) }
    func markAllAsRead(_ group: NotificationGroupKind) async throws {}
}
#endif
