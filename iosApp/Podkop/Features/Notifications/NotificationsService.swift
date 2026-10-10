import Foundation
import Observation
import PodkopShared

@MainActor protocol NotificationsLoading {
    func firstRequest(_ group: NotificationGroupKind) -> FeedRequest
    func load(_ group: NotificationGroupKind, request: FeedRequest, loaded: Int) async throws
        -> ListPage<AppNotification>
    func firstGroupRequest() -> FeedRequest
    /// The notifications inside group `groupID`, page by page, like website's "Rozwiń".
    func loadGroup(_ group: NotificationGroupKind, groupID: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<AppNotification>
    func refreshStatus() async throws
    func markAsRead(_ group: NotificationGroupKind, id: String) async throws
    func markAllAsRead(_ group: NotificationGroupKind) async throws
}

@MainActor
final class SharedNotificationsLoader: NotificationsLoading {
    private let client: PodkopClient
    private let adapter: BridgeAdapter

    init(client: PodkopClient, adapter: BridgeAdapter) {
        self.client = client
        self.adapter = adapter
    }

    func firstRequest(_ group: NotificationGroupKind) -> FeedRequest {
        FeedRequest(client.notifications.firstRequest(group: group.rawValue))
    }

    func load(_ group: NotificationGroupKind, request: FeedRequest, loaded: Int) async throws
        -> ListPage<AppNotification> {
        let page: IOSNotificationPage = try await adapter.call {
            self.client.notifications.load(group: group.rawValue,
                                           request: IOSPageRequest(kind: request.kind, value: request.value),
                                           loaded: Int32(loaded), completion: $0)
        }
        return Self.page(page, group: group)
    }

    func firstGroupRequest() -> FeedRequest {
        FeedRequest(client.notifications.firstGroupRequest())
    }

    func loadGroup(_ group: NotificationGroupKind, groupID: String, request: FeedRequest, loaded: Int) async throws
        -> ListPage<AppNotification> {
        let page: IOSNotificationPage = try await adapter.call {
            self.client.notifications.loadGroup(group: group.rawValue, groupId: groupID,
                                                request: IOSPageRequest(kind: request.kind, value: request.value),
                                                loaded: Int32(loaded), completion: $0)
        }
        return Self.page(page, group: group)
    }

    private static func page(_ page: IOSNotificationPage, group: NotificationGroupKind) -> ListPage<AppNotification> {
        ListPage(items: page.items.map { value in
            AppNotification(
                id: value.id, group: NotificationGroupKind(rawValue: value.group) ?? group, isRead: value.isRead,
                groupID: value.groupId, groupCount: Int(value.groupCount), showAsGroup: value.showAsGroup,
                groupUpdatedAt: value.groupUpdatedAtEpochMillis.map {
                    Date(timeIntervalSince1970: Double($0.int64Value) / 1000)
                },
                createdAt: Date(timeIntervalSince1970: Double(value.createdAtEpochMillis) / 1000),
                actor: value.actorUsername, actorAvatarURL: value.actorAvatarUrl, actorColor: value.actorColor,
                actorGender: value.actorGender,
                message: value.message, issueTitle: value.issueTitle, badgeName: value.badgeName,
                tagName: value.tagName, linkID: value.linkId?.intValue, linkTitle: value.linkTitle,
                entryID: value.entryId?.intValue, entryContent: value.entryContent,
                target: NotificationTargetValue(kind: value.targetKind, value: value.targetValue)
            )
        }, next: page.next.map(FeedRequest.init), total: page.total?.intValue)
    }

    func refreshStatus() async throws {
        let _: IOSNotificationStatus = try await adapter.call { self.client.notifications.refreshStatus(completion: $0) }
    }

    func markAsRead(_ group: NotificationGroupKind, id: String) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.notifications.markAsRead(group: group.rawValue, id: id, completion: $0)
        }
    }

    func markAllAsRead(_ group: NotificationGroupKind) async throws {
        let _: IOSSuccess = try await adapter.call {
            self.client.notifications.markAllAsRead(group: group.rawValue, completion: $0)
        }
    }
}
