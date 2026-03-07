package pl.masslany.podkop.business.notifications.data.network.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.DateAsStringSerializer
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto

@Serializable
data class NotificationsListDto(
    @SerialName("data")
    val data: List<NotificationDto>,
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)

@Serializable
data class NotificationItemResponseDto(
    @SerialName("data")
    val data: NotificationDto,
)

@Serializable
data class NotificationDto(
    @SerialName("type")
    val type: String? = null,
    @SerialName("id")
    val id: String,
    @SerialName("read")
    val read: Int = 0,
    @SerialName("group_id")
    val groupId: String? = null,
    @SerialName("group_count")
    val groupCount: Int? = null,
    @SerialName("show_as_group")
    val showAsGroup: Boolean? = null,
    @SerialName("created_at")
    @Serializable(with = DateAsStringSerializer::class)
    val createdAt: LocalDateTime,
    @SerialName("user")
    val user: NotificationUserDto? = null,
    @SerialName("profile")
    val profile: NotificationUserDto? = null,
    @SerialName("link")
    val link: NotificationResourceDto? = null,
    @SerialName("link_related")
    val linkRelated: NotificationResourceDto? = null,
    @SerialName("entry")
    val entry: NotificationResourceDto? = null,
    @SerialName("tag")
    val tag: NotificationTagDto? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("url")
    val url: String? = null,
    @SerialName("badge")
    val badge: NotificationBadgeDto? = null,
    @SerialName("issue")
    val issue: NotificationIssueDto? = null,
)

@Serializable
data class NotificationUserDto(
    @SerialName("username")
    val username: String,
    @SerialName("avatar")
    val avatar: String? = null,
    @SerialName("gender")
    val gender: String? = null,
    @SerialName("color")
    val color: String? = null,
)

@Serializable
data class NotificationResourceDto(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("content")
    val content: String? = null,
)

@Serializable
data class NotificationBadgeDto(
    @SerialName("name")
    val name: String? = null,
)

@Serializable
data class NotificationIssueDto(
    @SerialName("title")
    val title: String? = null,
)

@Serializable
data class NotificationTagDto(
    @SerialName("name")
    val name: String? = null,
)
