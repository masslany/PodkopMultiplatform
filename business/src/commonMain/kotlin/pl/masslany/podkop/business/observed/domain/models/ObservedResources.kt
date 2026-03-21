package pl.masslany.podkop.business.observed.domain.models

import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

data class ObservedResources(
    override val data: List<ObservedResource>,
    override val pagination: Pagination?,
) : PaginatedData<ObservedResource>
