package pl.masslany.podkop.business.hits.domain.main

import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.hits.domain.models.request.HitsSortType


interface HitsRepository {
    suspend fun getLinkHits(
        page: Any? = null,
        hitsSortType: HitsSortType,
        year: Int? = null,
        month: Int? = null,
    ): Result<Resources>
}
