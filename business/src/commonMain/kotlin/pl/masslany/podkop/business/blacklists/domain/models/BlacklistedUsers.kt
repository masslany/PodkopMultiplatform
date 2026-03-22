package pl.masslany.podkop.business.blacklists.domain.models

import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

data class BlacklistedUsers(
    override val data: List<BlacklistedUser>,
    override val pagination: Pagination?,
) : PaginatedData<BlacklistedUser>
