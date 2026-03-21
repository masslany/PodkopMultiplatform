package pl.masslany.podkop.business.rank.domain.models

import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

data class RankEntries(
    override val data: List<RankEntry>,
    override val pagination: Pagination?,
) : PaginatedData<RankEntry>
