package pl.masslany.podkop.features.linkdetails.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.linkdetails.LinkDetailsCommentsState
import pl.masslany.podkop.features.linkdetails.LinkDetailsRelatedState
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreenState
import pl.masslany.podkop.features.linkdetails.models.LinkDetailsCommentItemState
import pl.masslany.podkop.features.resources.models.related.RelatedItemState
import pl.masslany.podkop.features.resources.preview.LinkCommentItemStateProvider
import pl.masslany.podkop.features.resources.preview.LinkItemStateProvider

class LinkDetailsScreenStateProvider : PreviewParameterProvider<LinkDetailsScreenState> {
    private val link = LinkItemStateProvider().values.first()
    private val comment = LinkCommentItemStateProvider().values.first()

    override val values: Sequence<LinkDetailsScreenState> = sequenceOf(
        LinkDetailsScreenState.initial.copy(isLoading = true),
        LinkDetailsScreenState.initial.copy(isLoading = false, isError = true),
        LinkDetailsScreenState(
            isLoading = false,
            isError = false,
            isRefreshing = false,
            isLoggedIn = true,
            currentUsername = "patryk",
            link = link,
            commentsState = LinkDetailsCommentsState.Content(
                sortMenuState = DropdownMenuState(
                    items = persistentListOf(
                        DropdownMenuItemType.Newest,
                        DropdownMenuItemType.Oldest,
                        DropdownMenuItemType.Best,
                    ),
                    selected = DropdownMenuItemType.Newest,
                    expanded = false,
                ),
                comments = persistentListOf(
                    LinkDetailsCommentItemState(
                        id = 1,
                        comment = comment,
                        replies = comment.replies,
                        remainingRepliesCount = 0,
                        nextRepliesPage = null,
                        isLoadingReplies = false,
                    ),
                ),
                isPaginating = false,
            ),
            relatedState = LinkDetailsRelatedState.Content(
                items = persistentListOf(
                    RelatedItemState(
                        id = 1,
                        contentType = pl.masslany.podkop.features.resources.models.ResourceType.RelatedItem,
                        imageUrl = "https://picsum.photos/seed/related/400/300",
                        titleState = link.titleState,
                        authorState = link.authorState,
                        source = link.source,
                        sourceUrl = link.sourceUrl,
                        voteState = pl.masslany.podkop.common.preview.PreviewFixtures.voteState(),
                    ),
                ),
            ),
            isComposerVisible = false,
            composerContent = "",
            composerReplyTarget = null,
            composerParentCommentId = null,
            isComposerSubmitting = false,
        ),
        LinkDetailsScreenState(
            isLoading = false,
            isError = false,
            isRefreshing = true,
            isLoggedIn = false,
            currentUsername = null,
            link = link,
            commentsState = LinkDetailsCommentsState.Empty(
                sortMenuState = DropdownMenuState.initial,
            ),
            relatedState = LinkDetailsRelatedState.Empty,
            isComposerVisible = false,
            composerContent = "",
            composerReplyTarget = null,
            composerParentCommentId = null,
            isComposerSubmitting = false,
        ),
    )
}
