package pl.masslany.podkop.business.hits.data.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto


interface HitsDataSource {
    suspend fun getLinkHits(sort: String): Result<ResourceResponseDto>
}
