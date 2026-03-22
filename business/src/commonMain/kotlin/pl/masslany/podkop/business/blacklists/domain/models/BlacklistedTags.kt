package pl.masslany.podkop.business.blacklists.domain.models

import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

data class BlacklistedTags(
    override val data: List<BlacklistedTag>,
    override val pagination: Pagination?,
) : PaginatedData<BlacklistedTag>
