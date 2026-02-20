package pl.masslany.podkop.business.common.domain.models.common

interface PaginatedData<T> {
    val data: List<T>
    val pagination: Pagination?
}
