package pl.masslany.podkop.business.common.domain.models.common


data class Resources(
    override val data: List<ResourceItem>,
    override val pagination: Pagination?,
) : PaginatedData<ResourceItem>
