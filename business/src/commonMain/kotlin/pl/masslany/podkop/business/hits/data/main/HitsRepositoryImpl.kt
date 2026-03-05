package pl.masslany.podkop.business.hits.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.data.main.mapper.common.toResources
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.hits.data.api.HitsDataSource
import pl.masslany.podkop.business.hits.domain.main.HitsRepository
import pl.masslany.podkop.business.hits.domain.models.request.HitsSortType
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class HitsRepositoryImpl(
    private val hitsDataSource: HitsDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : HitsRepository {
    override suspend fun getLinkHits(
        page: Any?,
        hitsSortType: HitsSortType,
        year: Int?,
        month: Int?,
    ): Result<Resources> {
        return withContext(dispatcherProvider.io) {
            hitsDataSource.getLinkHits(
                page = page,
                sort = hitsSortType.value,
                year = year,
                month = month,
            ).mapCatching {
                it.toResources()
            }
        }
    }
}
