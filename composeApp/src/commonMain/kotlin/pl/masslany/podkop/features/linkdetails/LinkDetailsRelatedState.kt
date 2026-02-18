package pl.masslany.podkop.features.linkdetails

import kotlinx.collections.immutable.ImmutableList
import pl.masslany.podkop.features.resources.models.related.RelatedItemState

sealed interface LinkDetailsRelatedState {
    data object Loading : LinkDetailsRelatedState

    data object Error : LinkDetailsRelatedState

    data object Empty : LinkDetailsRelatedState

    data class Content(val items: ImmutableList<RelatedItemState>) : LinkDetailsRelatedState
}
