package pl.masslany.podkop.business.common.domain.models.common


data class Resources(
    val data: List<ResourceItem>,
    val pagination: Pagination?,
)
