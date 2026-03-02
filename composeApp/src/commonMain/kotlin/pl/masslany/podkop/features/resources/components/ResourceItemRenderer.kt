package pl.masslany.podkop.features.resources.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.preview.NoOpResourceItemActions
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.links.hits.HitItem
import pl.masslany.podkop.features.links.hits.models.HitItemState
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.resources.models.ResourceItemConfig
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState
import pl.masslany.podkop.features.resources.models.link.LinkItemState
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState
import pl.masslany.podkop.features.resources.preview.ResourceItemStateProvider
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.comment_button_show_comments

@Composable
fun ResourceItemRenderer(
    modifier: Modifier = Modifier,
    state: ResourceItemState,
    actions: ResourceItemActions,
    config: ResourceItemConfig = ResourceItemConfig(),
) {
    when (state) {
        is EntryItemState -> EntryItemRenderer(modifier, state, actions, config)
        is LinkItemState -> LinkItemRenderer(modifier, state, actions, config)
        is EntryCommentItemState -> EntryCommentItemRenderer(modifier, state, actions, config)
        is LinkCommentItemState -> LinkCommentItemRenderer(modifier, state, actions, config)
        is HitItemState -> HitItemRenderer(modifier, state, actions)
    }
}

@Composable
private fun EntryItemRenderer(
    modifier: Modifier,
    state: EntryItemState,
    actions: ResourceItemActions,
    config: ResourceItemConfig,
) {
    if (config.renderEntryAsCard) {
        Card(
            modifier = modifier
                .clip(CardDefaults.shape)
                .clickable { actions.onEntryClicked(state.id) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
            ),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
            ) {
                EntryItem(
                    state = state,
                    showInlineActions = config.showEntryInlineActions,
                    isReplyEnabled = config.isReplyActionEnabled,
                    onProfileClick = { actions.onProfileClicked(it) },
                    onTagClick = { actions.onTagClicked(it) },
                    onUrlClick = { actions.onLinkUrlClicked(it) },
                    onImageClick = { actions.onImageClicked(it) },
                    onEmbedPreviewClick = { embed -> actions.onEmbedPreviewClicked(state.id, embed) },
                    onVoteUpClick = {
                        actions.onEntryVoteUpClicked(
                            entryId = state.id,
                            voted = state.voteState.positiveVoteButtonState?.isVoted ?: false,
                        )
                    },
                    onReplyClick = if (config.showReplyAction) {
                        {
                            actions.onEntryReplyClicked(
                                entryId = state.id,
                                author = state.authorState?.name,
                            )
                        }
                    } else {
                        null
                    },
                    onMoreClick = { actions.onEntryMoreClicked(state.id) },
                )
                state.comments.forEach { comment ->
                    Spacer(Modifier.size(12.dp))
                    HorizontalDivider(
                        thickness = Dp.Hairline,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                    Spacer(Modifier.size(12.dp))
                    EntryCommentItem(
                        modifier = Modifier
                            .padding(
                                start = 16.dp,
                            ),
                        state = comment,
                        showInlineActions = config.showEntryInlineActions,
                        isReplyEnabled = config.isReplyActionEnabled,
                        onProfileClick = { actions.onProfileClicked(it) },
                        onTagClick = { actions.onTagClicked(it) },
                        onUrlClick = { actions.onLinkUrlClicked(it) },
                        onImageClick = { actions.onImageClicked(it) },
                        onEmbedPreviewClick = { embed -> actions.onEmbedPreviewClicked(comment.id, embed) },
                        onVoteUpClick = {
                            actions.onEntryCommentVoteUpClick(
                                entryCommentId = comment.id,
                                parentEntryId = comment.parentId,
                                voted = comment.voteState.positiveVoteButtonState?.isVoted ?: false,
                            )
                        },
                        onReplyClick = if (config.showReplyAction) {
                            {
                                actions.onEntryCommentReplyClicked(
                                    entryId = comment.parentId,
                                    entryCommentId = comment.id,
                                    author = comment.authorState?.name,
                                )
                            }
                        } else {
                            null
                        },
                        onMoreClick = {
                            actions.onEntryCommentMoreClicked(
                                entryId = comment.parentId,
                                entryCommentId = comment.id,
                            )
                        },
                    )
                }
                if (state.isShowCommentsButtonVisible) {
                    Spacer(Modifier.size(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        OutlinedButton(
                            onClick = { actions.onEntryClicked(state.id) },
                        ) {
                            Text(
                                text = stringResource(
                                    resource = Res.string.comment_button_show_comments,
                                    state.totalCommentsCount,
                                ),
                            )
                        }
                    }
                }
            }
        }
    } else {
        EntryItem(
            modifier = modifier
                .clickable { actions.onEntryClicked(state.id) },
            state = state,
            showInlineActions = config.showEntryInlineActions,
            isReplyEnabled = config.isReplyActionEnabled,
            onProfileClick = { actions.onProfileClicked(it) },
            onTagClick = { actions.onTagClicked(it) },
            onUrlClick = { actions.onLinkUrlClicked(it) },
            onImageClick = { actions.onImageClicked(it) },
            onEmbedPreviewClick = { embed -> actions.onEmbedPreviewClicked(state.id, embed) },
            onVoteUpClick = {
                actions.onEntryVoteUpClicked(
                    entryId = state.id,
                    voted = state.voteState.positiveVoteButtonState?.isVoted ?: false,
                )
            },
            onReplyClick = if (config.showReplyAction) {
                {
                    actions.onEntryReplyClicked(
                        entryId = state.id,
                        author = state.authorState?.name,
                    )
                }
            } else {
                null
            },
            onMoreClick = { actions.onEntryMoreClicked(state.id) },
        )
    }
}

@Composable
private fun LinkItemRenderer(
    modifier: Modifier,
    state: LinkItemState,
    actions: ResourceItemActions,
    config: ResourceItemConfig,
) {
    Column(
        modifier = modifier,
    ) {
        LinkItem(
            state = state,
            onLinkClick = { actions.onLinkClicked(state.id) },
            onVoteClick = { actions.onLinkVoteClicked(state.id, state.countState.isVoted) },
            onAuthorClick = { actions.onProfileClicked(it) },
            onTagClick = { actions.onTagClicked(it) },
            onSourceClick = { actions.onLinkUrlClicked(state.sourceUrl) },
            onProfileClicked = { actions.onProfileClicked(it) },
            onUrlClicked = { actions.onLinkUrlClicked(it) },
            onImageClicked = { actions.onImageClicked(it) },
            onEmbedPreviewClick = { embed -> actions.onEmbedPreviewClicked(state.id, embed) },
            onLinkCommentVoteUpClick = { linkId, commentId, voted ->
                actions.onLinkCommentVoteUpClick(
                    linkId = linkId,
                    commentId = commentId,
                    voted = voted,
                )
            },
            onLinkCommentMoreClick = { linkId, commentId, linkSlug, parentCommentId ->
                actions.onLinkCommentMoreClicked(
                    linkId = linkId,
                    commentId = commentId,
                    linkSlug = linkSlug,
                    parentCommentId = parentCommentId,
                )
            },
            onLinkCommentReplyClick = if (config.showLinkCommentReplyAction) {
                { linkId, commentId, author ->
                    actions.onLinkCommentReplyClicked(
                        linkId = linkId,
                        commentId = commentId,
                        author = author,
                    )
                }
            } else {
                null
            },
            isReplyEnabled = config.isReplyActionEnabled,
        )
    }
}

@Composable
private fun EntryCommentItemRenderer(
    modifier: Modifier,
    state: EntryCommentItemState,
    actions: ResourceItemActions,
    config: ResourceItemConfig,
) {
    EntryCommentItem(
        modifier = modifier,
        state = state,
        isReplyEnabled = config.isReplyActionEnabled,
        onProfileClick = { actions.onProfileClicked(it) },
        onTagClick = { actions.onTagClicked(it) },
        onUrlClick = { actions.onLinkUrlClicked(it) },
        onImageClick = { actions.onImageClicked(it) },
        onEmbedPreviewClick = { embed -> actions.onEmbedPreviewClicked(state.id, embed) },
        onVoteUpClick = {
            actions.onEntryCommentVoteUpClick(
                entryCommentId = state.id,
                parentEntryId = state.parentId,
                voted = state.voteState.positiveVoteButtonState?.isVoted ?: false,
            )
        },
        onReplyClick = if (config.showReplyAction) {
            {
                actions.onEntryCommentReplyClicked(
                    entryId = state.parentId,
                    entryCommentId = state.id,
                    author = state.authorState?.name,
                )
            }
        } else {
            null
        },
        onMoreClick = {
            actions.onEntryCommentMoreClicked(
                entryId = state.parentId,
                entryCommentId = state.id,
            )
        },
    )
}

@Composable
private fun HitItemRenderer(
    modifier: Modifier,
    state: HitItemState,
    actions: ResourceItemActions,
) {
    HitItem(
        modifier = modifier,
        state = state,
        onItemClick = { actions.onLinkClicked(state.id) },
        onVoteClick = { actions.onLinkVoteClicked(state.id, state.countState.isVoted) },
    )
}

@Composable
private fun LinkCommentItemRenderer(
    modifier: Modifier,
    state: LinkCommentItemState,
    actions: ResourceItemActions,
    config: ResourceItemConfig,
) {
    LinkCommentItem(
        modifier = modifier,
        state = state,
        isReplyEnabled = config.isReplyActionEnabled,
        onProfileClick = { actions.onProfileClicked(it) },
        onTagClick = { actions.onTagClicked(it) },
        onUrlClick = { actions.onLinkUrlClicked(it) },
        onImageClick = { actions.onImageClicked(it) },
        onEmbedPreviewClick = { embed -> actions.onEmbedPreviewClicked(state.id, embed) },
        onVoteUpClick = {
            actions.onLinkCommentVoteUpClick(
                linkId = state.linkId,
                commentId = state.id,
                voted = state.voteState.positiveVoteButtonState?.isVoted ?: false,
            )
        },
        onMoreClick = {
            actions.onLinkCommentMoreClicked(
                linkId = state.linkId,
                commentId = state.id,
                linkSlug = state.linkSlug,
                parentCommentId = state.parentCommentIdOrNull,
            )
        },
        onReplyClick = if (config.showLinkCommentReplyAction) {
            {
                actions.onLinkCommentReplyClicked(
                    linkId = state.linkId,
                    commentId = state.id,
                    author = state.authorState?.name,
                )
            }
        } else {
            null
        },
    )
}

@Preview
@Composable
private fun ResourceItemRendererPreview(
    @PreviewParameter(ResourceItemStateProvider::class) state: ResourceItemState,
) {
    PodkopPreview(darkTheme = false) {
        ResourceItemRenderer(
            modifier = Modifier.padding(16.dp),
            state = state,
            actions = NoOpResourceItemActions,
        )
    }
}
