package pl.masslany.podkop.business.profile.domain.models

import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

data class ObservedTags(
    override val data: List<ObservedTag>,
    override val pagination: Pagination?,
) : PaginatedData<ObservedTag>
