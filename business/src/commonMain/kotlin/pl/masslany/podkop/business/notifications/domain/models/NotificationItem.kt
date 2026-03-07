package pl.masslany.podkop.business.notifications.domain.models

import kotlinx.datetime.LocalDateTime
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor

data class NotificationItem(
    val id: String,
    val group: NotificationGroup,
    val type: String?,
    val isRead: Boolean,
    val groupId: String?,
    val groupCount: Int,
    val showAsGroup: Boolean,
    val createdAt: LocalDateTime,
    val actor: NotificationActor?,
    val message: String?,
    val url: String?,
    val tagName: String?,
    val profileUsername: String?,
    val entryId: Int?,
    val entryContent: String?,
    val linkId: Int?,
    val linkTitle: String?,
    val linkDescription: String?,
    val badgeName: String?,
    val issueTitle: String?,
)

data class NotificationActor(
    val username: String,
    val avatarUrl: String?,
    val gender: Gender,
    val nameColor: NameColor,
)
