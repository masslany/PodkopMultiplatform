package pl.masslany.podkop.features.resourceactions

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.UserItemState

data class ResourceVotesBottomSheetState(
    val isLoading: Boolean,
    val isError: Boolean,
    val isPaginating: Boolean,
    val isPaginationError: Boolean,
    val items: ImmutableList<UserItemState>,
) {
    companion object {
        val initial = ResourceVotesBottomSheetState(
            isLoading = true,
            isError = false,
            isPaginating = false,
            isPaginationError = false,
            items = persistentListOf(),
        )
    }
}

data class ResourceVotesParams(
    val resourceType: ResourceVotesType,
    val entryId: Int = 0,
    val entryCommentId: Int? = null,
    val linkId: Int = 0,
)
