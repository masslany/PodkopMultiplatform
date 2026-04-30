package pl.masslany.podkop.business.search.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.data.main.mapper.common.toResources
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.search.data.api.SearchDataSource
import pl.masslany.podkop.business.search.domain.main.SearchRepository
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class SearchRepositoryImpl(
    private val searchDataSource: SearchDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : SearchRepository {
    override suspend fun getSearchStream(
        page: Int,
        limit: Int?,
        query: SearchStreamQuery,
    ): Result<Resources> = withContext(dispatcherProvider.io) {
        searchDataSource.getSearchStream(
            page = page,
            limit = limit,
            query = query,
        ).mapCatching { response ->
            response.toResources()
                .withResolvedPagePagination(currentPage = page)
        }
    }

    private fun Resources.withResolvedPagePagination(currentPage: Int): Resources {
        val resolvedPagination = pagination?.resolveSearchNextPage(currentPage = currentPage)
        return copy(pagination = resolvedPagination)
    }

    private fun Pagination.resolveSearchNextPage(currentPage: Int): Pagination {
        if (next.isNotBlank()) {
            return this
        }

        if (perPage <= 0 || total <= perPage * currentPage) {
            return this.copy(next = "")
        }

        return this.copy(next = (currentPage + 1).toString())
    }
}
