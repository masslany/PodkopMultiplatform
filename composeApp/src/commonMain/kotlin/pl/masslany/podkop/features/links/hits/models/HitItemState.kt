package pl.masslany.podkop.features.links.hits.models

import pl.masslany.podkop.common.models.CountState
import pl.masslany.podkop.common.models.TitleState
import pl.masslany.podkop.features.resources.models.ResourceItemState
import pl.masslany.podkop.features.resources.models.ResourceType

data class HitItemState(
    override val id: Int,
    override val contentType: ResourceType,
    val titleState: TitleState,
    val countState: CountState,
    val imageUrl: String,
    val isAdult: Boolean,
) : ResourceItemState
