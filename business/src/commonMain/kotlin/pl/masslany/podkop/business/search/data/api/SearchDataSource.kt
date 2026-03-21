package pl.masslany.podkop.business.search.data.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery

interface SearchDataSource {
    suspend fun getSearchStream(
        page: Any?,
        limit: Int?,
        query: SearchStreamQuery,
    ): Result<ResourceResponseDto>
}
