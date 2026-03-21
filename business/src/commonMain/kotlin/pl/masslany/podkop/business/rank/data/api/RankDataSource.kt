package pl.masslany.podkop.business.rank.data.api

import pl.masslany.podkop.business.rank.data.network.models.RankResponseDto

interface RankDataSource {
    suspend fun getRank(page: Int): Result<RankResponseDto>
}
