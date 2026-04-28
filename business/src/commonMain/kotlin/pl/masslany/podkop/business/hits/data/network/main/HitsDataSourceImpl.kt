package pl.masslany.podkop.business.hits.data.network.main

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.hits.data.api.HitsDataSource
import pl.masslany.podkop.business.hits.data.network.api.HitsApi


class HitsDataSourceImpl(
    private val hitsApi: HitsApi,
) : HitsDataSource {
    override suspend fun getLinkHits(
        page: Int,
        sort: String,
        year: Int?,
        month: Int?,
    ): Result<ResourceResponseDto> {
        return hitsApi.getLinkHits(page, sort, year, month)
    }
}
