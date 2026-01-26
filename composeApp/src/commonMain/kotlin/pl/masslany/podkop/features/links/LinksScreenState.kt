package pl.masslany.podkop.features.links

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.features.links.hits.models.HitItemState
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class LinksScreenState(
    val isLoading: Boolean,
    val isUpcoming: Boolean,
    val links: ImmutableList<ResourceItemState>,
    val hits: ImmutableList<HitItemState>,
) {
    companion object {
        val initial = LinksScreenState(
            isLoading = false,
            isUpcoming = false,
            links = persistentListOf(),
            hits = persistentListOf(),
        )
    }

    fun showLoading() = this.copy(
        isLoading = true,
    )

    fun hideLoading() = this.copy(
        isLoading = false,
    )
}
