package pl.masslany.podkop.business.observed.domain.main

import pl.masslany.podkop.common.pagination.PageRequest

import pl.masslany.podkop.business.observed.domain.models.ObservedResources
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType

interface ObservedRepository {
    suspend fun getObserved(
        page: PageRequest,
        type: ObservedType,
    ): Result<ObservedResources>
}
