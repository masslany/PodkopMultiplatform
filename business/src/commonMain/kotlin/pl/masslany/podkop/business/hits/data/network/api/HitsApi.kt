package pl.masslany.podkop.business.hits.data.network.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto


interface HitsApi {
    suspend fun getLinkHits(sort: String): Result<ResourceResponseDto>
}
