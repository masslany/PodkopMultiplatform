package pl.masslany.podkop.features.privatemessages.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.components.AdultRating
import pl.masslany.podkop.common.components.Avatar
import pl.masslany.podkop.common.components.EmbedImage
import pl.masslany.podkop.common.components.EntryContent
import pl.masslany.podkop.common.components.Published
import pl.masslany.podkop.common.components.toComposeColor
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.privatemessages.models.ConversationMessageItemState
import pl.masslany.podkop.features.privatemessages.preview.PrivateMessagesPreviewFixtures
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.private_messages_embed_open

@Composable
internal fun ConversationMessageBubble(
    state: ConversationMessageItemState,
    onProfileClicked: (String) -> Unit,
    onTagClicked: (String) -> Unit,
    onUrlClicked: (String) -> Unit,
    onImageClicked: (String) -> Unit,
) {
    val bubbleColor = if (state.isIncoming) {
        MaterialTheme.colorScheme.surfaceContainerLow
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.align(
                if (state.isIncoming) Alignment.CenterStart else Alignment.CenterEnd,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            if (state.isIncoming) {
                Avatar(
                    state = state.senderAvatarState,
                    onClick = { state.senderName?.let(onProfileClicked) },
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .widthIn(max = 360.dp),
                color = bubbleColor,
                shape = if (state.isIncoming) {
                    RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                } else {
                    RoundedCornerShape(topStart = 18.dp, topEnd = 4.dp, bottomStart = 18.dp, bottomEnd = 18.dp)
                },
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.isIncoming && !state.senderName.isNullOrBlank()) {
                        Text(
                            text = state.senderName,
                            style = MaterialTheme.typography.labelLarge,
                            color = state.senderNameColorType.toComposeColor(),
                        )
                    }

                    if (state.adult) {
                        AdultRating()
                    }

                    state.contentState?.let { contentState ->
                        EntryContent(
                            state = contentState,
                            onProfileClick = onProfileClicked,
                            onTagClick = onTagClicked,
                            onUrlClick = onUrlClicked,
                        )
                    }

                    state.embedImageState?.let { embedImageState ->
                        EmbedImage(
                            state = embedImageState,
                            onImageClick = { onImageClicked(embedImageState.url) },
                            showSourceLabel = false,
                        )
                    }

                    state.embedUrl?.let { embedUrl ->
                        TextButton(
                            modifier = Modifier.align(Alignment.End),
                            onClick = { onUrlClicked(embedUrl) },
                        ) {
                            Text(text = stringResource(resource = Res.string.private_messages_embed_open))
                        }
                    }

                    Published(
                        modifier = Modifier.align(Alignment.End),
                        type = state.publishedAt,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ConversationMessageBubblePreview() {
    PodkopPreview(darkTheme = false) {
        ConversationMessageBubble(
            state = PrivateMessagesPreviewFixtures.conversationState().messages.first(),
            onProfileClicked = {},
            onTagClicked = {},
            onUrlClicked = {},
            onImageClicked = {},
        )
    }
}
