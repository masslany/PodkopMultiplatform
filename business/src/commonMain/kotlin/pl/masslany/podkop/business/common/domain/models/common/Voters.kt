package pl.masslany.podkop.business.common.domain.models.common

data class Voters(
    override val data: List<Voter>,
    override val pagination: Pagination?,
) : PaginatedData<Voter>
