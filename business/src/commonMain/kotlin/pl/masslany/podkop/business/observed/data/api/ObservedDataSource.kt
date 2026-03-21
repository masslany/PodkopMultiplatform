package pl.masslany.podkop.business.observed.data.api

import pl.masslany.podkop.business.observed.data.network.models.ObservedResponseDto
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType

interface ObservedDataSource {
    suspend fun getObserved(
        page: Any?,
        type: ObservedType,
    ): Result<ObservedResponseDto>
}
