package pl.masslany.podkop.business.observed.data.network.main

import pl.masslany.podkop.common.pagination.PageRequest

import pl.masslany.podkop.business.observed.data.api.ObservedDataSource
import pl.masslany.podkop.business.observed.data.network.api.ObservedApi
import pl.masslany.podkop.business.observed.data.network.models.ObservedResponseDto
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType

class ObservedDataSourceImpl(
    private val observedApi: ObservedApi,
) : ObservedDataSource {
    override suspend fun getObserved(
        page: PageRequest,
        type: ObservedType,
    ): Result<ObservedResponseDto> {
        return observedApi.getObserved(page = page, type = type)
    }
}
