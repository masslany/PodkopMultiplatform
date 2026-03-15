package pl.masslany.podkop.features.resourceactions

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.UserItemState

data class ResourceVotesBottomSheetState(
    val isLoading: Boolean = true,
    val isError: Boolean = false,
    val isPaginating: Boolean = false,
    val items: ImmutableList<UserItemState> = persistentListOf(),
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
