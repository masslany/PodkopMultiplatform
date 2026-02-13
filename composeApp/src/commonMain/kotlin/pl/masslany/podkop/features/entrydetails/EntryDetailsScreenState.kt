package pl.masslany.podkop.features.entrydetails

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class EntryDetailsScreenState(
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val entry: ResourceItemState?,
    val comments: ImmutableList<ResourceItemState>,
    val isPaginating: Boolean,
) {
    companion object Companion {
        val initial = EntryDetailsScreenState(
            isLoading = true,
            isRefreshing = false,
            entry = null,
            comments = persistentListOf(),
            isPaginating = false,
        )
    }

    fun updateLoading(isLoading: Boolean) = this.copy(
        isLoading = isLoading,
    )

    fun updateRefreshing(isRefreshing: Boolean) = this.copy(
        isRefreshing = isRefreshing,
    )
}
