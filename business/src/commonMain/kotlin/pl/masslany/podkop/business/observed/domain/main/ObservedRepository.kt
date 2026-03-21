package pl.masslany.podkop.business.observed.domain.main

import pl.masslany.podkop.business.observed.domain.models.ObservedResources
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType

interface ObservedRepository {
    suspend fun getObserved(
        page: Any?,
        type: ObservedType,
    ): Result<ObservedResources>
}
