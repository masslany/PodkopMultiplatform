package pl.masslany.podkop.features.linkdetails

import kotlinx.collections.immutable.ImmutableList
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.linkdetails.models.LinkDetailsCommentItemState

sealed interface LinkDetailsCommentsState {
    val sortMenuState: DropdownMenuState

    data class Loading(override val sortMenuState: DropdownMenuState) : LinkDetailsCommentsState

    data class Error(override val sortMenuState: DropdownMenuState) : LinkDetailsCommentsState

    data class Empty(override val sortMenuState: DropdownMenuState) : LinkDetailsCommentsState

    data class Content(
        override val sortMenuState: DropdownMenuState,
        val comments: ImmutableList<LinkDetailsCommentItemState>,
        val isPaginating: Boolean,
    ) : LinkDetailsCommentsState
}
