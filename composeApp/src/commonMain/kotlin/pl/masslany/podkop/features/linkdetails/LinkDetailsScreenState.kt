package pl.masslany.podkop.features.linkdetails

import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.resources.models.link.LinkItemState

data class LinkDetailsScreenState(
    val isLoading: Boolean,
    val isError: Boolean,
    val isRefreshing: Boolean,
    val isLoggedIn: Boolean,
    val currentUsername: String?,
    val link: LinkItemState?,
    val downvoteMenuState: LinkDownvoteMenuState,
    val commentsState: LinkDetailsCommentsState,
    val relatedState: LinkDetailsRelatedState,
) {
    companion object {
        val initial = LinkDetailsScreenState(
            isLoading = true,
            isError = false,
            isRefreshing = false,
            isLoggedIn = false,
            currentUsername = null,
            link = null,
            downvoteMenuState = LinkDownvoteMenuState.initial,
            commentsState = LinkDetailsCommentsState.Loading(
                sortMenuState = DropdownMenuState.initial,
            ),
            relatedState = LinkDetailsRelatedState.Loading,
        )
    }
}
