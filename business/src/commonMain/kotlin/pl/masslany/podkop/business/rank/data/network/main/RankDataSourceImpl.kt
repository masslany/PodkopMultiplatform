package pl.masslany.podkop.business.rank.data.network.main

import pl.masslany.podkop.business.rank.data.api.RankDataSource
import pl.masslany.podkop.business.rank.data.network.api.RankApi
import pl.masslany.podkop.business.rank.data.network.models.RankResponseDto

class RankDataSourceImpl(
    private val rankApi: RankApi,
) : RankDataSource {
    override suspend fun getRank(page: Int): Result<RankResponseDto> {
        return rankApi.getRank(page = page)
    }
}
