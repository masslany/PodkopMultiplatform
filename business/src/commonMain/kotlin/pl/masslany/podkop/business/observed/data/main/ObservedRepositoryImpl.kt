package pl.masslany.podkop.business.observed.data.main

import pl.masslany.podkop.common.pagination.PageRequest

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.observed.data.api.ObservedDataSource
import pl.masslany.podkop.business.observed.data.main.mapper.toResources
import pl.masslany.podkop.business.observed.domain.main.ObservedRepository
import pl.masslany.podkop.business.observed.domain.models.ObservedResources
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class ObservedRepositoryImpl(
    private val observedDataSource: ObservedDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : ObservedRepository {
    override suspend fun getObserved(
        page: PageRequest,
        type: ObservedType,
    ): Result<ObservedResources> {
        return withContext(dispatcherProvider.io) {
            observedDataSource.getObserved(page = page, type = type).mapCatching { response ->
                response.toResources()
            }
        }
    }
}
