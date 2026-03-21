package pl.masslany.podkop.business.observed.domain.models

import pl.masslany.podkop.business.common.domain.models.common.ResourceItem

data class ObservedResource(
    val item: ResourceItem,
    val newContentCount: Int? = null,
)
