package pl.masslany.podkop.business.hits.data.network.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto


interface HitsApi {
    suspend fun getLinkHits(
        page: Any? = null,
        sort: String,
        year: Int? = null,
        month: Int? = null,
    ): Result<ResourceResponseDto>
}
