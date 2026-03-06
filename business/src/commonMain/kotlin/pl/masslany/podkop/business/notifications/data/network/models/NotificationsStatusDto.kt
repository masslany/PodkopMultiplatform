package pl.masslany.podkop.business.notifications.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationsStatusDto(
    @SerialName("data")
    val data: NotificationsStatusDataDto,
)

@Serializable
data class NotificationsStatusDataDto(
    @SerialName("pm")
    val pm: Boolean,
    @SerialName("pm_notification")
    val pmNotification: Boolean,
    @SerialName("entry_notification")
    val entryNotification: Boolean,
    @SerialName("tag_notification")
    val tagNotification: Boolean,
    @SerialName("observed_discussions_notification")
    val observedDiscussionsNotification: Boolean,
    @SerialName("pm_notification_count")
    val pmNotificationCount: Int,
    @SerialName("entry_notification_count")
    val entryNotificationCount: Int,
    @SerialName("tag_notification_count")
    val tagNotificationCount: Int,
    @SerialName("observed_discussions_notification_count")
    val observedDiscussionsNotificationCount: Int,
)
