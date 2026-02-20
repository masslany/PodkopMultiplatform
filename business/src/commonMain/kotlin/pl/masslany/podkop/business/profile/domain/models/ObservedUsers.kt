package pl.masslany.podkop.business.profile.domain.models

import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

data class ObservedUsers(
    override val data: List<ObservedUser>,
    override val pagination: Pagination?,
) : PaginatedData<ObservedUser>
