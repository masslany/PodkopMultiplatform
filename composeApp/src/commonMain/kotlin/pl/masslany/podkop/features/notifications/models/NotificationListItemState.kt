package pl.masslany.podkop.features.notifications.models

import kotlinx.collections.immutable.ImmutableList
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType

data class NotificationListItemState(
    val id: String,
    val isRead: Boolean,
    val actorName: String?,
    val actorAvatarUrl: String?,
    val actorNameColorType: NameColorType,
    val actorGenderIndicatorType: GenderIndicatorType,
    val publishedAt: PublishedTimeType,
    val notificationIds: ImmutableList<String>,
    val headline: String?,
    val groupCount: Int,
    val tagName: String?,
    val groupedTagContentType: GroupedTagContentType?,
    val observedResourceType: ObservedNotificationResourceType?,
    val observedResourceTitle: String?,
    val navigationTarget: NotificationNavigationTarget,
    /** Set when the row stands for a group of notifications, which can be expanded. */
    val groupId: String? = null,
    /** The group's own notifications, once expanded; null while collapsed. */
    val expansion: NotificationGroupExpansionState? = null,
)

data class NotificationGroupExpansionState(
    val items: ImmutableList<NotificationListItemState>,
    val isLoading: Boolean,
    val canLoadMore: Boolean,
)

enum class GroupedTagContentType {
    Entry,
    Link,
    Generic,
}

enum class ObservedNotificationResourceType {
    Entry,
    Link,
}
