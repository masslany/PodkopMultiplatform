package pl.masslany.podkop.features.linkdetails.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.common.preview.PreviewFixtures
import pl.masslany.podkop.features.linkdetails.LinkDetailsCommentsState
import pl.masslany.podkop.features.linkdetails.LinkDetailsRelatedState
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreenState
import pl.masslany.podkop.features.linkdetails.LinkDownvoteMenuState
import pl.masslany.podkop.features.linkdetails.models.LinkDetailsCommentItemState
import pl.masslany.podkop.features.resources.models.related.RelatedItemState
import pl.masslany.podkop.features.resources.preview.LinkCommentItemStateProvider
import pl.masslany.podkop.features.resources.preview.LinkItemStateProvider

class LinkDetailsScreenStateProvider : PreviewParameterProvider<LinkDetailsScreenState> {
    private val link = LinkItemStateProvider().values.first().copy(
        authorState = PreviewFixtures.authorState(name = "link_author"),
    )
    private val comment = LinkCommentItemStateProvider().values.first()
    private val authorComment = comment.copy(
        authorState = PreviewFixtures.authorState(name = "link_author"),
    )
    private val parentAuthorReply = comment.copy(
        id = 2,
        authorState = PreviewFixtures.authorState(name = "thread_parent"),
        replies = persistentListOf(
            comment.copy(
                id = 3,
                parentId = 2,
                authorState = PreviewFixtures.authorState(name = "thread_parent"),
            ),
        ),
    )
    private val currentUserComment = comment.copy(
        id = 4,
        authorState = PreviewFixtures.authorState(name = "patryk"),
    )

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
            downvoteMenuState = LinkDownvoteMenuState.initial,
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
                        comment = authorComment,
                        replies = authorComment.replies,
                        remainingRepliesCount = 0,
                        nextRepliesPage = null,
                        isLoadingReplies = false,
                    ),
                    LinkDetailsCommentItemState(
                        id = 2,
                        comment = parentAuthorReply,
                        replies = parentAuthorReply.replies,
                        remainingRepliesCount = 0,
                        nextRepliesPage = null,
                        isLoadingReplies = false,
                    ),
                    LinkDetailsCommentItemState(
                        id = 4,
                        comment = currentUserComment,
                        replies = currentUserComment.replies,
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
        ),
        LinkDetailsScreenState(
            isLoading = false,
            isError = false,
            isRefreshing = true,
            isLoggedIn = false,
            currentUsername = null,
            link = link,
            downvoteMenuState = LinkDownvoteMenuState.initial,
            commentsState = LinkDetailsCommentsState.Empty(
                sortMenuState = DropdownMenuState.initial,
            ),
            relatedState = LinkDetailsRelatedState.Empty,
        ),
    )
}
