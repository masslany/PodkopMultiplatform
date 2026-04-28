package pl.masslany.podkop.business.observed.data.network.api

import pl.masslany.podkop.common.pagination.PageRequest

import pl.masslany.podkop.business.observed.data.network.models.ObservedResponseDto
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType

interface ObservedApi {
    suspend fun getObserved(
        page: PageRequest,
        type: ObservedType,
    ): Result<ObservedResponseDto>
}
