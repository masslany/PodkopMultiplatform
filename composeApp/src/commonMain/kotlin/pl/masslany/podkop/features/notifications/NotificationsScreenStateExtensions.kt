package pl.masslany.podkop.features.notifications

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.common.models.avatar.toGenderIndicatorType
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.common.models.toPublishedTimeType
import pl.masslany.podkop.features.notifications.models.GroupedTagContentType
import pl.masslany.podkop.features.notifications.models.NotificationListItemState
import pl.masslany.podkop.features.notifications.models.NotificationNavigationTarget
import pl.masslany.podkop.features.notifications.models.ObservedNotificationResourceType

internal fun List<NotificationItem>.toNotificationItemStates(
    selectedGroup: NotificationGroup,
): List<NotificationListItemState> = when (selectedGroup) {
    NotificationGroup.Tags -> toGroupedTagNotificationStates()
    NotificationGroup.ObservedDiscussions -> toObservedDiscussionNotificationStates()
    else -> map(NotificationItem::toNotificationListItemState)
}

private fun List<NotificationItem>.toGroupedTagNotificationStates(): List<NotificationListItemState> =
    groupBy { notification ->
        notification.groupId
            ?.takeIf { notification.groupCount > 1 }
            ?: notification.id
    }
        .values
        .map { groupedNotifications ->
            val representative = groupedNotifications.first()
            if (
                representative.groupCount > 1 &&
                !representative.groupId.isNullOrBlank() &&
                !representative.tagName.isNullOrBlank()
            ) {
                representative.toGroupedTagState(groupedNotifications)
            } else {
                representative.toNotificationListItemState()
            }
        }

private fun List<NotificationItem>.toObservedDiscussionNotificationStates(): List<NotificationListItemState> =
    groupBy { notification ->
        notification.groupId
            ?.takeIf { notification.groupCount > 1 }
            ?: notification.id
    }
        .values
        .map { groupedNotifications ->
            val representative = groupedNotifications.first()
            if (
                representative.groupCount > 1 &&
                !representative.groupId.isNullOrBlank() &&
                representative.observedResourceType != null
            ) {
                representative.toGroupedObservedDiscussionState(groupedNotifications)
            } else {
                representative.toNotificationListItemState()
            }
        }

private fun NotificationItem.toNotificationListItemState(): NotificationListItemState = NotificationListItemState(
    id = id,
    isRead = isRead,
    actorName = actor?.username,
    actorAvatarUrl = actor?.avatarUrl,
    actorNameColorType = actor?.nameColor?.toNameColorType() ?: NotificationsDefaults.NameColor,
    actorGenderIndicatorType = actor?.gender?.toGenderIndicatorType() ?: NotificationsDefaults.GenderIndicator,
    publishedAt = createdAt.toPublishedTimeType(),
    notificationIds = if (group == NotificationGroup.PrivateMessages) {
        persistentListOf()
    } else {
        persistentListOf(id)
    },
    headline = headline(),
    groupCount = groupCount,
    tagName = tagName,
    groupedTagContentType = null,
    observedResourceType = observedResourceType,
    observedResourceTitle = observedResourceTitle,
    navigationTarget = navigationTarget(),
)

private fun NotificationItem.toGroupedTagState(
    groupedNotifications: List<NotificationItem>,
): NotificationListItemState = NotificationListItemState(
    id = groupId?.let { "group:$it" } ?: id,
    isRead = groupedNotifications.all(NotificationItem::isRead),
    actorName = null,
    actorAvatarUrl = null,
    actorNameColorType = NotificationsDefaults.NameColor,
    actorGenderIndicatorType = NotificationsDefaults.GenderIndicator,
    publishedAt = createdAt.toPublishedTimeType(),
    notificationIds = groupedNotifications
        .map(NotificationItem::id)
        .toPersistentList(),
    headline = null,
    groupCount = groupCount,
    tagName = tagName,
    groupedTagContentType = groupedTagContentType(groupedNotifications),
    observedResourceType = null,
    observedResourceTitle = null,
    navigationTarget = NotificationNavigationTarget.Tag(tagName.orEmpty()),
)

private fun NotificationItem.toGroupedObservedDiscussionState(
    groupedNotifications: List<NotificationItem>,
): NotificationListItemState = NotificationListItemState(
    id = groupId?.let { "group:$it" } ?: id,
    isRead = groupedNotifications.all(NotificationItem::isRead),
    actorName = actor?.username,
    actorAvatarUrl = actor?.avatarUrl,
    actorNameColorType = actor?.nameColor?.toNameColorType() ?: NotificationsDefaults.NameColor,
    actorGenderIndicatorType = actor?.gender?.toGenderIndicatorType() ?: NotificationsDefaults.GenderIndicator,
    publishedAt = createdAt.toPublishedTimeType(),
    notificationIds = groupedNotifications
        .map(NotificationItem::id)
        .toPersistentList(),
    headline = null,
    groupCount = groupCount,
    tagName = null,
    groupedTagContentType = null,
    observedResourceType = observedResourceType,
    observedResourceTitle = observedResourceTitle,
    navigationTarget = navigationTarget(),
)

private fun groupedTagContentType(
    groupedNotifications: List<NotificationItem>,
): GroupedTagContentType = when {
    groupedNotifications.all { it.linkId != null && it.entryId == null } -> GroupedTagContentType.Link
    groupedNotifications.all { it.entryId != null && it.linkId == null } -> GroupedTagContentType.Entry
    else -> GroupedTagContentType.Generic
}

private fun NotificationItem.headline(): String? = listOf(
    message,
    issueTitle,
    badgeName,
    linkTitle,
    entryContent,
).firstNotNullOfOrNull { it }

private val NotificationItem.observedResourceType: ObservedNotificationResourceType?
    get() = when {
        linkId != null -> ObservedNotificationResourceType.Link
        entryId != null -> ObservedNotificationResourceType.Entry
        else -> null
    }

private val NotificationItem.observedResourceTitle: String?
    get() = when (observedResourceType) {
        ObservedNotificationResourceType.Link -> linkTitle
        ObservedNotificationResourceType.Entry -> entryContent
        null -> null
    }

private object NotificationsDefaults {
    val GenderIndicator = GenderIndicatorType.Unspecified
    val NameColor = NameColorType.Orange
}
