package pl.masslany.podkop.business.entries.domain.models

import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

data class EntryVoters(
    override val data: List<EntryVoter>,
    override val pagination: Pagination?,
) : PaginatedData<EntryVoter>
