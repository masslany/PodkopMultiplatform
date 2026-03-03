package pl.masslany.podkop.features.resourceactions

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.features.profile.models.ProfileObservedUserItemState

data class ResourceVotesBottomSheetState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val isPaginating: Boolean = false,
    val items: ImmutableList<ProfileObservedUserItemState> = persistentListOf(),
) {
    companion object {
        val initial = ResourceVotesBottomSheetState()
    }
}

data class ResourceVotesParams(
    val resourceType: ResourceVotesType,
    val entryId: Int = 0,
    val entryCommentId: Int? = null,
    val linkId: Int = 0,
)
