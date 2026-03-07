package pl.masslany.podkop.features.notifications.preview

import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.features.notifications.NotificationsScreenState
import pl.masslany.podkop.features.notifications.models.GroupedTagContentType
import pl.masslany.podkop.features.notifications.models.NotificationGroupChipState
import pl.masslany.podkop.features.notifications.models.NotificationListItemState
import pl.masslany.podkop.features.notifications.models.NotificationNavigationTarget
import pl.masslany.podkop.features.notifications.models.ObservedNotificationResourceType

internal object NotificationsPreviewFixtures {
    fun groups(selectedGroup: NotificationGroup = NotificationGroup.ObservedDiscussions) = persistentListOf(
        NotificationGroupChipState(
            group = NotificationGroup.Entries,
            unreadCount = 3,
            selected = selectedGroup == NotificationGroup.Entries,
        ),
        NotificationGroupChipState(
            group = NotificationGroup.PrivateMessages,
            unreadCount = 1,
            selected = selectedGroup == NotificationGroup.PrivateMessages,
        ),
        NotificationGroupChipState(
            group = NotificationGroup.Tags,
            unreadCount = 11,
            selected = selectedGroup == NotificationGroup.Tags,
        ),
        NotificationGroupChipState(
            group = NotificationGroup.ObservedDiscussions,
            unreadCount = 4,
            selected = selectedGroup == NotificationGroup.ObservedDiscussions,
        ),
    )

    fun regularNotification() = NotificationListItemState(
        id = "regular",
        isRead = false,
        actorName = "jagielanka",
        actorAvatarUrl = "https://picsum.photos/seed/notifications-avatar/96/96",
        actorNameColorType = NameColorType.Orange,
        actorGenderIndicatorType = GenderIndicatorType.Female,
        publishedAt = PublishedTimeType.Minutes(3),
        notificationIds = persistentListOf("regular"),
        headline = "Twoje znalezisko zostało dodane do wykopaliska",
        groupCount = 1,
        tagName = null,
        groupedTagContentType = null,
        observedResourceType = null,
        observedResourceTitle = null,
        navigationTarget = NotificationNavigationTarget.Link(id = 1234),
    )

    fun groupedTagNotification() = NotificationListItemState(
        id = "tag-group",
        isRead = false,
        actorName = null,
        actorAvatarUrl = null,
        actorNameColorType = NameColorType.Orange,
        actorGenderIndicatorType = GenderIndicatorType.Unspecified,
        publishedAt = PublishedTimeType.Minutes(9),
        notificationIds = persistentListOf("tag-1", "tag-2", "tag-3"),
        headline = null,
        groupCount = 8,
        tagName = "wojna",
        groupedTagContentType = GroupedTagContentType.Entry,
        observedResourceType = null,
        observedResourceTitle = null,
        navigationTarget = NotificationNavigationTarget.Tag(name = "wojna"),
    )

    fun groupedObservedNotification() = NotificationListItemState(
        id = "observed-group",
        isRead = false,
        actorName = null,
        actorAvatarUrl = null,
        actorNameColorType = NameColorType.Orange,
        actorGenderIndicatorType = GenderIndicatorType.Unspecified,
        publishedAt = PublishedTimeType.Now,
        notificationIds = persistentListOf("obs-1", "obs-2"),
        headline = null,
        groupCount = 2,
        tagName = null,
        groupedTagContentType = null,
        observedResourceType = ObservedNotificationResourceType.Entry,
        observedResourceTitle = "Wracam do Was z prawdopodobnie już ostatnim wpisem...",
        navigationTarget = NotificationNavigationTarget.Entry(id = 85261721),
    )

    fun observedSingleNotification() = NotificationListItemState(
        id = "observed-single",
        isRead = false,
        actorName = "TheMan",
        actorAvatarUrl = "https://picsum.photos/seed/observed-avatar/96/96",
        actorNameColorType = NameColorType.Orange,
        actorGenderIndicatorType = GenderIndicatorType.Male,
        publishedAt = PublishedTimeType.Now,
        notificationIds = persistentListOf("obs-single"),
        headline = null,
        groupCount = 1,
        tagName = null,
        groupedTagContentType = null,
        observedResourceType = ObservedNotificationResourceType.Link,
        observedResourceTitle = "Rafah było miastem w południowej części Strefy Gazy",
        navigationTarget = NotificationNavigationTarget.Link(id = 7902567),
    )

    fun contentState(
        selectedGroup: NotificationGroup = NotificationGroup.ObservedDiscussions,
    ) = NotificationsScreenState.initial.copy(
        isLoading = false,
        selectedGroup = selectedGroup,
        groups = groups(selectedGroup),
        items = when (selectedGroup) {
            NotificationGroup.Tags -> persistentListOf(groupedTagNotification(), regularNotification())
            NotificationGroup.ObservedDiscussions -> persistentListOf(
                observedSingleNotification(),
                groupedObservedNotification(),
            )

            else -> persistentListOf(regularNotification())
        },
    )
}
