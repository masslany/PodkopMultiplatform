package pl.masslany.podkop.business.rank.data.network.api

import pl.masslany.podkop.business.rank.data.network.models.RankResponseDto

interface RankApi {
    suspend fun getRank(page: Int): Result<RankResponseDto>
}
