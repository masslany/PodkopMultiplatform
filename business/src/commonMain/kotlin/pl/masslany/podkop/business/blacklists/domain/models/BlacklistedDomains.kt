package pl.masslany.podkop.business.blacklists.domain.models

import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

data class BlacklistedDomains(
    override val data: List<BlacklistedDomain>,
    override val pagination: Pagination?,
) : PaginatedData<BlacklistedDomain>
