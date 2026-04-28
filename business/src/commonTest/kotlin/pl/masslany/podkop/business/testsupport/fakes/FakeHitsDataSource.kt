package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.hits.data.api.HitsDataSource

class FakeHitsDataSource : HitsDataSource {
    data class GetLinkHitsCall(
        val page: Int,
        val sort: String,
        val year: Int?,
        val month: Int?,
    )

    var getLinkHitsResult: Result<ResourceResponseDto> = unstubbedResult("HitsDataSource.getLinkHits")
    val getLinkHitsCalls = mutableListOf<GetLinkHitsCall>()

    override suspend fun getLinkHits(
        page: Int,
        sort: String,
        year: Int?,
        month: Int?,
    ): Result<ResourceResponseDto> {
        getLinkHitsCalls += GetLinkHitsCall(
            page = page,
            sort = sort,
            year = year,
            month = month,
        )
        return getLinkHitsResult
    }
}
