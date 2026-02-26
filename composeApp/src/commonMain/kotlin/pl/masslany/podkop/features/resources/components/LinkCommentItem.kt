package pl.masslany.podkop.features.resources.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.components.Author
import pl.masslany.podkop.common.components.Avatar
import pl.masslany.podkop.common.components.EmbedImage
import pl.masslany.podkop.common.components.EntryContent
import pl.masslany.podkop.common.components.Published
import pl.masslany.podkop.common.components.embed.EmbedContent
import pl.masslany.podkop.common.components.vote.Vote
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState
import pl.masslany.podkop.features.resources.preview.LinkCommentItemStateProvider

@Composable
fun LinkCommentItem(
    state: LinkCommentItemState,
    modifier: Modifier = Modifier,
    showInlineActions: Boolean = true,
    onProfileClick: (String) -> Unit,
    onTagClick: (String) -> Unit,
    onUrlClick: (String) -> Unit,
    onVoteUpClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onEmbedPreviewClick: (EmbedContentState) -> Unit,
    onMoreClick: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Row {
            Row(
                modifier = Modifier.weight(1f),
            ) {
                Avatar(
                    state = state.avatarState,
                    onClick = { onProfileClick(state.authorState?.name.orEmpty()) },
                )
                Spacer(Modifier.size(8.dp))
                Column {
                    state.authorState?.let {
                        Author(
                            state = state.authorState,
                            onClick = { onProfileClick(state.authorState.name) },
                        )
                    }
                    state.publishedTimeType?.let {
                        Published(
                            type = state.publishedTimeType,
                        )
                    }
                }
            }
            Vote(
                state = state.voteState,
                onVoteUpClick = onVoteUpClick,
                onVoteDownClick = { /* no-op */ },
            )
        }
        Spacer(Modifier.size(8.dp))
        EntryContent(
            state = state.entryContentState,
            onProfileClick = onProfileClick,
            onTagClick = onTagClick,
            onUrlClick = onUrlClick,
        )
        state.embedImageState?.let {
            Spacer(Modifier.size(8.dp))
            EmbedImage(
                state = state.embedImageState,
                onImageClick = { onImageClick(state.embedImageState.url) },
            )
        }
        state.embedContentState?.let {
            Spacer(Modifier.size(8.dp))
            EmbedContent(
                state = state.embedContentState,
                onPreviewClick = { onEmbedPreviewClick(state.embedContentState) },
                onFetchedContentClick = { onUrlClick(state.embedContentState.url) },
            )
        }
        if (showInlineActions) {
            Spacer(Modifier.size(2.dp))
            ResourceInlineActionsRow(onMoreClick = onMoreClick)
        }
    }
}

@Preview
@Composable
private fun LinkCommentItemPreview(
    @PreviewParameter(LinkCommentItemStateProvider::class) state: LinkCommentItemState,
) {
    PodkopPreview(darkTheme = false) {
        LinkCommentItem(
            modifier = Modifier,
            state = state,
            onProfileClick = {},
            onTagClick = {},
            onUrlClick = {},
            onVoteUpClick = {},
            onImageClick = {},
            onEmbedPreviewClick = {},
            onMoreClick = {},
        )
    }
}
