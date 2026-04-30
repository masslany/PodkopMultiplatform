package pl.masslany.podkop.business.search.data.network.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery

interface SearchApi {
    suspend fun getSearchStream(
        page: Int,
        limit: Int?,
        query: SearchStreamQuery,
    ): Result<ResourceResponseDto>
}
