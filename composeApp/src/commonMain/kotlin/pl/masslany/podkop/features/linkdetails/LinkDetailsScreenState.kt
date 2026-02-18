package pl.masslany.podkop.features.linkdetails

import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.resources.models.link.LinkItemState

data class LinkDetailsScreenState(
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val link: LinkItemState?,
    val commentsState: LinkDetailsCommentsState,
    val relatedState: LinkDetailsRelatedState,
) {
    companion object Companion {
        val initial = LinkDetailsScreenState(
            isLoading = true,
            isRefreshing = false,
            link = null,
            commentsState = LinkDetailsCommentsState.Loading(
                sortMenuState = DropdownMenuState.initial,
            ),
            relatedState = LinkDetailsRelatedState.Loading,
        )
    }
}
