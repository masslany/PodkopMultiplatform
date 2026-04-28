package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.search.data.api.SearchDataSource
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery

class FakeSearchDataSource : SearchDataSource {
    data class GetSearchStreamCall(
        val page: Int,
        val limit: Int?,
        val query: SearchStreamQuery,
    )

    var getSearchStreamResult: Result<ResourceResponseDto> = unstubbedResult("SearchDataSource.getSearchStream")

    val getSearchStreamCalls = mutableListOf<GetSearchStreamCall>()

    override suspend fun getSearchStream(
        page: Int,
        limit: Int?,
        query: SearchStreamQuery,
    ): Result<ResourceResponseDto> {
        getSearchStreamCalls += GetSearchStreamCall(
            page = page,
            limit = limit,
            query = query,
        )
        return getSearchStreamResult
    }
}
