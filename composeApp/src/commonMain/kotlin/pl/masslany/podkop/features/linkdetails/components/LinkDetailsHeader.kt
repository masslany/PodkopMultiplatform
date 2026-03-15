package pl.masslany.podkop.features.linkdetails.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest.Builder
import coil3.request.crossfade
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.components.Author
import pl.masslany.podkop.common.components.Count
import pl.masslany.podkop.common.components.Description
import pl.masslany.podkop.common.components.Dot
import pl.masslany.podkop.common.components.Source
import pl.masslany.podkop.common.components.Tag
import pl.masslany.podkop.common.components.Title
import pl.masslany.podkop.common.models.vote.VoteReasonType
import pl.masslany.podkop.common.models.vote.toStringResource
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.linkdetails.LinkDownvoteMenuState
import pl.masslany.podkop.features.resources.components.ResourceInlineActionsRow
import pl.masslany.podkop.features.resources.models.link.LinkItemState
import pl.masslany.podkop.features.resources.preview.LinkItemStateProvider
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.link_details_downvote_label
import podkop.composeapp.generated.resources.link_details_downvote_undo_label

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LinkDetailsHeader(
    modifier: Modifier = Modifier,
    state: LinkItemState,
    isReplyEnabled: Boolean,
    downvoteMenuState: LinkDownvoteMenuState,
    onLinkClick: () -> Unit,
    onVoteClick: () -> Unit,
    onDownvoteClick: () -> Unit,
    onDownvoteReasonSelected: (VoteReasonType) -> Unit,
    onDownvoteDismissed: () -> Unit,
    onFavouriteClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onTagClick: (String) -> Unit,
    onReplyClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                onLinkClick()
            },
        ) {
            if (state.imageUrl.isNotEmpty()) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    model = Builder(LocalPlatformContext.current)
                        .data(state.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Count(
                        state = state.countState,
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        onClick = onVoteClick,
                    )
                    if (downvoteMenuState.isVisible) {
                        Spacer(modifier = Modifier.size(4.dp))
                        LinkDownvoteButton(
                            isDownVoted = state.isDownVoted,
                            menuState = downvoteMenuState,
                            onClick = onDownvoteClick,
                            onReasonSelected = onDownvoteReasonSelected,
                            onDismissRequest = onDownvoteDismissed,
                        )
                    }
                }
                state.titleState?.let {
                    Spacer(modifier = Modifier.size(8.dp))
                    Title(state = state.titleState)
                }
            }
            state.descriptionState?.let {
                Spacer(modifier = Modifier.size(8.dp))
                Description(
                    state = state.descriptionState,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            state.authorState?.let {
                Author(state = state.authorState, onClick = onAuthorClick)
                Spacer(modifier = Modifier.size(4.dp))
                Dot()
            }
            state.source?.let {
                Spacer(modifier = Modifier.size(4.dp))
                Source(source = state.source, onSourceClick = onLinkClick)
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            state.tags.forEach { tag ->
                Tag(state = tag, onTagClick = onTagClick)
                if (tag.needsSpacer) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Dot()
                    Spacer(modifier = Modifier.size(4.dp))
                }
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        ResourceInlineActionsRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            isReplyEnabled = isReplyEnabled,
            onFavouriteClick = onFavouriteClick,
            isFavourite = state.isFavourite,
            isFavouriteEnabled = state.isFavouriteEnabled,
            onReplyClick = onReplyClick,
            onMoreClick = onMoreClick,
        )
        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Composable
private fun LinkDownvoteButton(
    isDownVoted: Boolean,
    menuState: LinkDownvoteMenuState,
    onClick: () -> Unit,
    onReasonSelected: (VoteReasonType) -> Unit,
    onDismissRequest: () -> Unit,
) {
    Box {
        TextButton(
            modifier = Modifier.height(24.dp),
            onClick = onClick,
            enabled = !menuState.isSubmitting,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDownVoted) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                contentColor = if (isDownVoted) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            ),
            contentPadding = PaddingValues(
                vertical = 4.dp,

            ),
        ) {
            if (menuState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(
                text = stringResource(
                    resource = if (isDownVoted) {
                        Res.string.link_details_downvote_undo_label
                    } else {
                        Res.string.link_details_downvote_label
                    },
                ),
                style = MaterialTheme.typography.labelSmall,
            )
        }
        DropdownMenu(
            expanded = menuState.expanded,
            onDismissRequest = onDismissRequest,
        ) {
            menuState.reasons.forEach { reason ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(reason.toStringResource()),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    onClick = { onReasonSelected(reason) },
                    enabled = !menuState.isSubmitting,
                )
            }
        }
    }
}

@Preview
@Composable
private fun LinkDetailsHeaderPreview(
    @PreviewParameter(LinkItemStateProvider::class) state: LinkItemState,
) {
    PodkopPreview(darkTheme = false) {
        LinkDetailsHeader(
            modifier = Modifier.padding(bottom = 16.dp),
            state = state,
            isReplyEnabled = true,
            downvoteMenuState = LinkDownvoteMenuState.initial.copy(isVisible = true),
            onLinkClick = {},
            onVoteClick = {},
            onDownvoteClick = {},
            onDownvoteReasonSelected = {},
            onDownvoteDismissed = {},
            onFavouriteClick = {},
            onAuthorClick = {},
            onTagClick = {},
            onReplyClick = {},
            onMoreClick = {},
        )
    }
}

@Preview
@Composable
private fun LinkDetailsHeaderPreviewNoDownvote(
    @PreviewParameter(LinkItemStateProvider::class) state: LinkItemState,
) {
    PodkopPreview(darkTheme = false) {
        LinkDetailsHeader(
            modifier = Modifier.padding(bottom = 16.dp),
            state = state,
            isReplyEnabled = true,
            downvoteMenuState = LinkDownvoteMenuState.initial,
            onLinkClick = {},
            onVoteClick = {},
            onDownvoteClick = {},
            onDownvoteReasonSelected = {},
            onDownvoteDismissed = {},
            onFavouriteClick = {},
            onAuthorClick = {},
            onTagClick = {},
            onReplyClick = {},
            onMoreClick = {},
        )
    }
}
