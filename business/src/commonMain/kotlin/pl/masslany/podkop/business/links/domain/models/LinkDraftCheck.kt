package pl.masslany.podkop.business.links.domain.models

import pl.masslany.podkop.business.common.domain.models.common.ResourceItem

data class LinkDraftCheck(
    val key: String,
    val similar: List<ResourceItem>,
    val duplicate: Boolean,
)
