package pl.masslany.podkop.business.rank.domain.main

import pl.masslany.podkop.business.rank.domain.models.RankEntries

interface RankRepository {
    suspend fun getRank(page: Int = 1): Result<RankEntries>
}
