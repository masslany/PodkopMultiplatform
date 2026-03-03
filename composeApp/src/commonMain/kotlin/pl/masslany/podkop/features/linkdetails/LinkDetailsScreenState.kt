package pl.masslany.podkop.features.linkdetails

import pl.masslany.podkop.common.composer.ComposerState
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.resources.models.link.LinkItemState

data class LinkDetailsScreenState(
    val isLoading: Boolean,
    val isError: Boolean,
    val isRefreshing: Boolean,
    val isLoggedIn: Boolean,
    val currentUsername: String?,
    val link: LinkItemState?,
    val commentsState: LinkDetailsCommentsState,
    val relatedState: LinkDetailsRelatedState,
    val composer: ComposerState,
) {
    companion object {
        val initial = LinkDetailsScreenState(
            isLoading = true,
            isError = false,
            isRefreshing = false,
            isLoggedIn = false,
            currentUsername = null,
            link = null,
            commentsState = LinkDetailsCommentsState.Loading(
                sortMenuState = DropdownMenuState.initial,
            ),
            relatedState = LinkDetailsRelatedState.Loading,
            composer = ComposerState.initial,
        )
    }

    fun updateComposer(transform: (ComposerState) -> ComposerState): LinkDetailsScreenState = copy(composer = transform(composer))
}
