package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.hits.data.api.HitsDataSource

class FakeHitsDataSource : HitsDataSource {
    var getLinkHitsResult: Result<ResourceResponseDto> = unstubbedResult("HitsDataSource.getLinkHits")
    val getLinkHitsCalls = mutableListOf<String>()

    override suspend fun getLinkHits(sort: String): Result<ResourceResponseDto> {
        getLinkHitsCalls += sort
        return getLinkHitsResult
    }
}
