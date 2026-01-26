package pl.masslany.podkop.features.links.hits.models

import pl.masslany.podkop.common.models.CountState
import pl.masslany.podkop.common.models.TitleState
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class HitItemState(
    override val id: Int,
    val titleState: TitleState,
    val countState: CountState,
    val imageUrl: String,
    val isAdult: Boolean,
) : ResourceItemState
