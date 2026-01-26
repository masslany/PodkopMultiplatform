package pl.masslany.podkop.business.hits.domain.main

import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.hits.domain.models.request.HitsSortType


interface HitsRepository {
    suspend fun getLinkHits(hitsSortType: HitsSortType): Result<Resources>
}
