package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto
import pl.masslany.podkop.business.common.domain.models.common.Pagination


fun PaginationDto.toPagination(): Pagination {
    return Pagination(
        perPage = this.perPage ?: 0,
        total = this.total ?: this.totalItems ?: 0,
        next = this.next.orEmpty(),
        prev = this.prev.orEmpty(),
    )
}
