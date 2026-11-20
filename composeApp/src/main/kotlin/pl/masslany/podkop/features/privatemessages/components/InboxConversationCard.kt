package pl.masslany.podkop.features.privatemessages.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.components.Avatar
import pl.masslany.podkop.common.components.Published
import pl.masslany.podkop.common.components.toComposeColor
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.privatemessages.models.InboxConversationItemState
import pl.masslany.podkop.features.privatemessages.preview.PrivateMessagesPreviewFixtures

@Composable
internal fun InboxConversationCard(
    state: InboxConversationItemState,
    onClick: () -> Unit,
) {
    val containerColor = if (state.unread) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Avatar(
                state = state.avatarState,
                onClick = onClick,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = state.username,
                        style = MaterialTheme.typography.titleSmall,
                        color = state.nameColorType.toComposeColor(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (state.unread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.extraSmall,
                                ),
                        )
                    }
                }

                if (state.lastMessagePreview.isNotBlank()) {
                    Text(
                        text = state.lastMessagePreview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    androidx.compose.material3.HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                    )
                }

                Published(type = state.publishedAt)
            }
        }
    }
}

@Preview
@Composable
private fun InboxConversationCardPreview() {
    PodkopPreview(darkTheme = false) {
        InboxConversationCard(
            state = PrivateMessagesPreviewFixtures.inboxState().conversations.first(),
            onClick = {},
        )
    }
}
