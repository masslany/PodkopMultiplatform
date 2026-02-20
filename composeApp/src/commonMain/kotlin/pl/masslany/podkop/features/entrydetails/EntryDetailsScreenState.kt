package pl.masslany.podkop.features.entrydetails

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class EntryDetailsScreenState(
    val isLoading: Boolean,
    val isError: Boolean,
    val isRefreshing: Boolean,
    val isCommentsError: Boolean,
    val entry: ResourceItemState?,
    val comments: ImmutableList<ResourceItemState>,
    val isPaginating: Boolean,
) {
    companion object Companion {
        val initial = EntryDetailsScreenState(
            isLoading = true,
            isError = false,
            isRefreshing = false,
            isCommentsError = false,
            entry = null,
            comments = persistentListOf(),
            isPaginating = false,
        )
    }

    fun updateLoading(isLoading: Boolean) = this.copy(
        isLoading = isLoading,
    )

    fun updateError(isError: Boolean) = this.copy(
        isError = isError,
    )

    fun updateCommentsError(isCommentsContextError: Boolean) = this.copy(
        isCommentsError = isCommentsContextError,
    )

    fun updateRefreshing(isRefreshing: Boolean) = this.copy(
        isRefreshing = isRefreshing,
    )
}
