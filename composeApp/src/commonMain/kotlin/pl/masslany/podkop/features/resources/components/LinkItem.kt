package pl.masslany.podkop.features.resources.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import pl.masslany.podkop.common.components.Author
import pl.masslany.podkop.common.components.CommentCount
import pl.masslany.podkop.common.components.Count
import pl.masslany.podkop.common.components.Description
import pl.masslany.podkop.common.components.Dot
import pl.masslany.podkop.common.components.Published
import pl.masslany.podkop.common.components.Source
import pl.masslany.podkop.common.components.Tag
import pl.masslany.podkop.common.components.Title
import pl.masslany.podkop.common.components.embed.EmbedContent
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.resourceactions.resourceTextSelectionGesture
import pl.masslany.podkop.features.resources.models.link.LinkItemState
import pl.masslany.podkop.features.resources.preview.LinkItemStateProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LinkItem(
    modifier: Modifier = Modifier,
    state: LinkItemState,
    onLinkClick: () -> Unit,
    onVoteClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onTagClick: (String) -> Unit,
    onSourceClick: () -> Unit,
    onProfileClicked: (String) -> Unit,
    onUrlClicked: (String) -> Unit,
    onImageClicked: (String) -> Unit,
    onEmbedPreviewClick: (EmbedContentState) -> Unit,
    onLinkCommentVoteUpClick: (linkId: Int, commentId: Int, voted: Boolean) -> Unit,
    onLinkCommentVoteDownClick: (linkId: Int, commentId: Int, voted: Boolean) -> Unit,
    onLinkCommentFavouriteClick: (linkId: Int, commentId: Int, favourited: Boolean) -> Unit,
    onLinkCommentLongClick: (linkId: Int, commentId: Int) -> Unit,
    onLinkCommentMoreClick: (
        linkId: Int,
        commentId: Int,
        linkSlug: String,
        parentCommentId: Int?,
    ) -> Unit,
    onLinkCommentReplyClick: ((linkId: Int, commentId: Int, author: String?) -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .clip(CardDefaults.shape)
            .clickable { onLinkClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        ),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                    ),
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Count(
                        state = state.countState,
                        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        onClick = onVoteClick,
                    )
                }
                state.titleState?.let {
                    Spacer(modifier = Modifier.size(12.dp))
                    Title(state = state.titleState)
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    state.descriptionState?.let {
                        Description(
                            modifier = Modifier.weight(1f),
                            state = state.descriptionState,
                        )
                    }
                    if (state.imageUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.size(8.dp))
                        AsyncImage(
                            modifier = Modifier
                                .height(80.dp)
                                .width(80.dp)
                                .clip(MaterialTheme.shapes.small),
                            model = ImageRequest.Builder(LocalPlatformContext.current)
                                .data(state.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                state.embedContentState?.let {
                    EmbedContent(
                        state = state.embedContentState,
                        onPreviewClick = { onEmbedPreviewClick(state.embedContentState) },
                        onFetchedContentClick = { onUrlClicked(state.embedContentState.url) },
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                FlowRow {
                    state.authorState?.let {
                        Author(
                            state = state.authorState,
                            onClick = onAuthorClick,
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Dot()
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    state.source?.let {
                        Source(
                            source = state.source,
                            onSourceClick = onSourceClick,
                        )
                    }
                    state.publishedTimeType?.let {
                        Spacer(modifier = Modifier.size(4.dp))
                        Dot()
                        Spacer(modifier = Modifier.size(4.dp))
                        Published(type = state.publishedTimeType)
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                ) {
                    FlowRow(
                        modifier = Modifier
                            .weight(1f),
                    ) {
                        state.tags.forEach { tag ->
                            Tag(
                                state = tag,
                                onTagClick = onTagClick,
                            )
                            if (tag.needsSpacer) {
                                Spacer(modifier = Modifier.size(4.dp))
                                Dot()
                                Spacer(modifier = Modifier.size(4.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    CommentCount(count = state.commentCount)
                }
                if (state.comments.isEmpty()) {
                    Spacer(modifier = Modifier.size(16.dp))
                }
            }
            if (state.comments.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                ) {
                    state.comments.forEachIndexed { index, comment ->
                        HorizontalDivider(
                            thickness = Dp.Hairline,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        )
                        Spacer(Modifier.size(8.dp))
                        LinkCommentItem(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .resourceTextSelectionGesture(
                                    onClick = onLinkClick,
                                    onLongClick = if (
                                        comment.entryContentState is pl.masslany.podkop.common.models.EntryContentState.Content &&
                                        !comment.isBlacklisted &&
                                        comment.rawContent.isNotBlank()
                                    ) {
                                        { onLinkCommentLongClick(comment.linkId, comment.id) }
                                    } else {
                                        null
                                    },
                                ),
                            state = comment,
                            onProfileClick = { onProfileClicked(it) },
                            onTagClick = { onTagClick(it) },
                            onUrlClick = { onUrlClicked(it) },
                            onImageClick = { onImageClicked(it) },
                            onVoteUpClick = {
                                onLinkCommentVoteUpClick(
                                    comment.linkId,
                                    comment.id,
                                    comment.voteState.positiveVoteButtonState?.isVoted ?: false,
                                )
                            },
                            onVoteDownClick = {
                                onLinkCommentVoteDownClick(
                                    comment.linkId,
                                    comment.id,
                                    comment.voteState.negativeVoteButtonState?.isVoted ?: false,
                                )
                            },
                            onFavouriteClick = {
                                onLinkCommentFavouriteClick(
                                    comment.linkId,
                                    comment.id,
                                    comment.isFavourite,
                                )
                            },
                            onEmbedPreviewClick = { embed -> onEmbedPreviewClick(embed) },
                            onMoreClick = {
                                onLinkCommentMoreClick(
                                    comment.linkId,
                                    comment.id,
                                    comment.linkSlug,
                                    comment.parentCommentIdOrNull,
                                )
                            },
                            onReplyClick = if (onLinkCommentReplyClick != null) {
                                {
                                    onLinkCommentReplyClick(
                                        comment.linkId,
                                        comment.id,
                                        comment.authorState?.name,
                                    )
                                }
                            } else {
                                null
                            },
                        )
                        if (index != state.comments.lastIndex) {
                            Spacer(Modifier.size(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LinkItemPreview(
    @PreviewParameter(LinkItemStateProvider::class) state: LinkItemState,
) {
    PodkopPreview(darkTheme = false) {
        LinkItem(
            modifier = Modifier.padding(16.dp),
            state = state,
            onLinkClick = {},
            onVoteClick = {},
            onAuthorClick = {},
            onTagClick = {},
            onSourceClick = {},
            onProfileClicked = {},
            onUrlClicked = {},
            onImageClicked = {},
            onEmbedPreviewClick = {},
            onLinkCommentVoteUpClick = { _, _, _ -> },
            onLinkCommentVoteDownClick = { _, _, _ -> },
            onLinkCommentFavouriteClick = { _, _, _ -> },
            onLinkCommentLongClick = { _, _ -> },
            onLinkCommentMoreClick = { _, _, _, _ -> },
        )
    }
}
