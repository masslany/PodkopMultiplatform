package pl.masslany.podkop.features.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.common.components.AvatarImageTypeRouter
import pl.masslany.podkop.common.components.GenderIndicator
import pl.masslany.podkop.common.components.Published
import pl.masslany.podkop.common.components.toComposeColor
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.notifications.models.GroupedTagContentType
import pl.masslany.podkop.features.notifications.models.NotificationListItemState
import pl.masslany.podkop.features.notifications.models.ObservedNotificationResourceType
import pl.masslany.podkop.features.notifications.preview.NotificationsPreviewFixtures
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_comment
import podkop.composeapp.generated.resources.notifications_fallback_title
import podkop.composeapp.generated.resources.notifications_observed_entry_comment_action
import podkop.composeapp.generated.resources.notifications_observed_entry_comments_action
import podkop.composeapp.generated.resources.notifications_observed_link_comment_action
import podkop.composeapp.generated.resources.notifications_observed_link_comments_action
import podkop.composeapp.generated.resources.notifications_tag_grouped_entries_action
import podkop.composeapp.generated.resources.notifications_tag_grouped_generic_action
import podkop.composeapp.generated.resources.notifications_tag_grouped_links_action

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    state: NotificationListItemState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (state.isRead) {
        MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                if (state.groupedTagContentType != null) {
                    GroupedTagMarker()
                } else if (state.isGroupedObservedDiscussion()) {
                    GroupedCommentMarker()
                } else {
                    NotificationAvatar(
                        avatarUrl = state.actorAvatarUrl,
                        genderIndicatorType = state.actorGenderIndicatorType,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (state.groupedTagContentType == null && !state.isGroupedObservedDiscussion()) {
                            state.actorName?.let { actorName ->
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = actorName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = state.actorNameColorType.toComposeColor(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f))
                        }

                        if (!state.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(MaterialTheme.colorScheme.primary),
                            )
                        }
                    }

                    Published(type = state.publishedAt)
                }
            }

            Text(
                text = state.title(),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GroupedTagMarker(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "#",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NotificationAvatar(
    avatarUrl: String?,
    genderIndicatorType: GenderIndicatorType,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AvatarImageTypeRouter(
            avatarType = avatarUrl
                ?.takeIf { url -> url.isNotBlank() }
                ?.let(AvatarType::NetworkImage)
                ?: AvatarType.NoAvatar,
        )
        GenderIndicator(
            type = genderIndicatorType,
            modifier = Modifier
                .width(36.dp)
                .height(2.dp),
        )
    }
}

@Composable
private fun NotificationListItemState.title(): AnnotatedString {
    val actionColor = MaterialTheme.colorScheme.onSurfaceVariant
    val contentColor = MaterialTheme.colorScheme.onSurface

    val plainTitle = headline ?: stringResource(resource = Res.string.notifications_fallback_title)

    return when (groupedTagContentType) {
        GroupedTagContentType.Entry -> styledNotificationTitle(
            action = pluralStringResource(
                resource = Res.plurals.notifications_tag_grouped_entries_action,
                quantity = groupCount,
                groupCount,
            ),
            content = "#${tagName.orEmpty()}",
            actionColor = actionColor,
            contentColor = contentColor,
        )

        GroupedTagContentType.Link -> styledNotificationTitle(
            action = pluralStringResource(
                resource = Res.plurals.notifications_tag_grouped_links_action,
                quantity = groupCount,
                groupCount,
            ),
            content = "#${tagName.orEmpty()}",
            actionColor = actionColor,
            contentColor = contentColor,
        )

        GroupedTagContentType.Generic -> styledNotificationTitle(
            action = pluralStringResource(
                resource = Res.plurals.notifications_tag_grouped_generic_action,
                quantity = groupCount,
                groupCount,
            ),
            content = "#${tagName.orEmpty()}",
            actionColor = actionColor,
            contentColor = contentColor,
        )

        null -> when (observedResourceType) {
            ObservedNotificationResourceType.Entry -> styledNotificationTitle(
                action = if (notificationIds.size > 1) {
                    pluralStringResource(
                        resource = Res.plurals.notifications_observed_entry_comments_action,
                        quantity = groupCount,
                        groupCount,
                    )
                } else {
                    stringResource(resource = Res.string.notifications_observed_entry_comment_action)
                },
                content = observedResourceTitle.orEmpty(),
                actionColor = actionColor,
                contentColor = contentColor,
            )

            ObservedNotificationResourceType.Link -> styledNotificationTitle(
                action = if (notificationIds.size > 1) {
                    pluralStringResource(
                        resource = Res.plurals.notifications_observed_link_comments_action,
                        quantity = groupCount,
                        groupCount,
                    )
                } else {
                    stringResource(resource = Res.string.notifications_observed_link_comment_action)
                },
                content = observedResourceTitle.orEmpty(),
                actionColor = actionColor,
                contentColor = contentColor,
            )

            null -> AnnotatedString(plainTitle)
        }
    }
}

private fun styledNotificationTitle(
    action: String,
    content: String,
    actionColor: Color,
    contentColor: Color,
): AnnotatedString = buildAnnotatedString {
    withStyle(SpanStyle(color = actionColor)) {
        append(action)
    }

    if (content.isNotBlank()) {
        append('\n')
        withStyle(SpanStyle(color = contentColor)) {
            append(content)
        }
    }
}

private fun NotificationListItemState.isGroupedObservedDiscussion(): Boolean =
    observedResourceType != null && notificationIds.size > 1

@Composable
private fun GroupedCommentMarker(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Icon(
            modifier = Modifier
                .size(20.dp)
                .align(Alignment.Center),
            imageVector = vectorResource(resource = Res.drawable.ic_comment),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(name = "Regular Notification")
@Composable
private fun NotificationCardRegularPreview() {
    PodkopPreview(darkTheme = false) {
        NotificationCard(
            state = NotificationsPreviewFixtures.regularNotification(),
            onClick = {},
        )
    }
}

@Preview(name = "Grouped Tag Notification")
@Composable
private fun NotificationCardGroupedTagPreview() {
    PodkopPreview(darkTheme = false) {
        NotificationCard(
            state = NotificationsPreviewFixtures.groupedTagNotification(),
            onClick = {},
        )
    }
}

@Preview(name = "Grouped Observed Discussion")
@Composable
private fun NotificationCardGroupedObservedPreview() {
    PodkopPreview(darkTheme = false) {
        NotificationCard(
            state = NotificationsPreviewFixtures.groupedObservedNotification(),
            onClick = {},
        )
    }
}
