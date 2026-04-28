package pl.masslany.podkop.business.search.domain.main

import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery

interface SearchRepository {
    suspend fun getSearchStream(
        page: Int,
        limit: Int?,
        query: SearchStreamQuery,
    ): Result<Resources>
}
