package pl.masslany.podkop.business.search.data.network.main

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.search.data.api.SearchDataSource
import pl.masslany.podkop.business.search.data.network.api.SearchApi
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery

class SearchDataSourceImpl(
    private val searchApi: SearchApi,
) : SearchDataSource {
    override suspend fun getSearchStream(
        page: Any?,
        limit: Int?,
        query: SearchStreamQuery,
    ): Result<ResourceResponseDto> = searchApi.getSearchStream(
        page = page,
        limit = limit,
        query = query,
    )
}
