package pl.masslany.podkop.business.notifications.data.main

import pl.masslany.podkop.business.common.data.main.mapper.common.toPagination
import pl.masslany.podkop.business.common.data.main.mapper.toGender
import pl.masslany.podkop.business.common.data.main.mapper.toNameColor
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.notifications.data.network.models.NotificationDto
import pl.masslany.podkop.business.notifications.data.network.models.NotificationItemResponseDto
import pl.masslany.podkop.business.notifications.data.network.models.NotificationUserDto
import pl.masslany.podkop.business.notifications.data.network.models.NotificationsListDto
import pl.masslany.podkop.business.notifications.domain.models.NotificationActor
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.business.notifications.domain.models.NotificationItem
import pl.masslany.podkop.business.notifications.domain.models.NotificationsPage

internal fun NotificationsListDto.toNotificationsPage(group: NotificationGroup): NotificationsPage =
    NotificationsPage(
        data = data.map { it.toNotificationItem(group = group) },
        pagination = pagination?.toPagination(),
    )

internal fun NotificationItemResponseDto.toNotificationItem(group: NotificationGroup): NotificationItem =
    data.toNotificationItem(group = group)

private fun NotificationDto.toNotificationItem(group: NotificationGroup): NotificationItem = NotificationItem(
    id = id,
    group = group,
    type = type,
    isRead = read != 0,
    groupId = groupId,
    groupCount = groupCount ?: 1,
    showAsGroup = showAsGroup == true,
    createdAt = createdAt,
    actor = (user ?: profile)?.toNotificationActor(),
    message = message,
    url = url,
    tagName = tag?.name,
    profileUsername = profile?.username ?: user?.username,
    entryId = entry?.id,
    entryContent = entry?.content,
    linkId = link?.id ?: linkRelated?.id,
    linkTitle = link?.title ?: linkRelated?.title,
    linkDescription = link?.description,
    badgeName = badge?.name,
    issueTitle = issue?.title,
)

private fun NotificationUserDto.toNotificationActor(): NotificationActor = NotificationActor(
    username = username,
    avatarUrl = avatar,
    gender = gender.toGender(),
    nameColor = color?.toNameColor() ?: NameColor.Orange,
)
