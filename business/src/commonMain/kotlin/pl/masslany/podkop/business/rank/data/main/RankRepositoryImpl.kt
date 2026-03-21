package pl.masslany.podkop.business.rank.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.business.rank.data.api.RankDataSource
import pl.masslany.podkop.business.rank.data.main.mapper.toRankEntries
import pl.masslany.podkop.business.rank.domain.main.RankRepository
import pl.masslany.podkop.business.rank.domain.models.RankEntries
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class RankRepositoryImpl(
    private val rankDataSource: RankDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : RankRepository {
    override suspend fun getRank(page: Int): Result<RankEntries> = withContext(dispatcherProvider.io) {
        rankDataSource.getRank(page = page)
            .mapCatching { response ->
                response.toRankEntries()
                    .withResolvedPagePagination(currentPage = page)
            }
    }

    private fun RankEntries.withResolvedPagePagination(currentPage: Int): RankEntries {
        val resolvedPagination = pagination?.resolveNextPage(currentPage = currentPage)
        return copy(pagination = resolvedPagination)
    }

    private fun Pagination.resolveNextPage(currentPage: Int): Pagination {
        if (next.isNotBlank()) {
            return this
        }

        if (perPage <= 0 || total <= perPage * currentPage) {
            return copy(next = "")
        }

        return copy(next = (currentPage + 1).toString())
    }
}
