package pl.masslany.podkop.business.blacklists.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.blacklists.data.api.BlacklistsDataSource
import pl.masslany.podkop.business.blacklists.data.main.mapper.toBlacklistedDomains
import pl.masslany.podkop.business.blacklists.data.main.mapper.toBlacklistedTags
import pl.masslany.podkop.business.blacklists.data.main.mapper.toBlacklistedUsers
import pl.masslany.podkop.business.blacklists.domain.main.BlacklistsRepository
import pl.masslany.podkop.business.common.data.main.mapper.common.toPagination
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class BlacklistsRepositoryImpl(
    private val blacklistsDataSource: BlacklistsDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : BlacklistsRepository {
    override suspend fun getBlacklistedUsers(page: Int) =
        withContext(dispatcherProvider.io) {
            blacklistsDataSource.getBlacklistedUsers(page = page)
                .mapCatching { response ->
                    response.toBlacklistedUsers()
                        .copy(pagination = response.pagination?.toPaginationWithResolvedNextPage(currentPage = page))
                }
        }

    override suspend fun getBlacklistedTags(page: Int) =
        withContext(dispatcherProvider.io) {
            blacklistsDataSource.getBlacklistedTags(page = page)
                .mapCatching { response ->
                    response.toBlacklistedTags()
                        .copy(pagination = response.pagination?.toPaginationWithResolvedNextPage(currentPage = page))
                }
        }

    override suspend fun getBlacklistedDomains(page: Int) =
        withContext(dispatcherProvider.io) {
            blacklistsDataSource.getBlacklistedDomains(page = page)
                .mapCatching { response ->
                    response.toBlacklistedDomains()
                        .copy(pagination = response.pagination?.toPaginationWithResolvedNextPage(currentPage = page))
                }
        }

    override suspend fun addBlacklistedUser(username: String): Result<Unit> =
        withContext(dispatcherProvider.io) {
            blacklistsDataSource.addBlacklistedUser(username)
        }

    override suspend fun removeBlacklistedUser(username: String): Result<Unit> =
        withContext(dispatcherProvider.io) {
            blacklistsDataSource.removeBlacklistedUser(username)
        }

    override suspend fun addBlacklistedTag(tag: String): Result<Unit> =
        withContext(dispatcherProvider.io) {
            blacklistsDataSource.addBlacklistedTag(tag)
        }

    override suspend fun removeBlacklistedTag(tag: String): Result<Unit> =
        withContext(dispatcherProvider.io) {
            blacklistsDataSource.removeBlacklistedTag(tag)
        }

    override suspend fun addBlacklistedDomain(domain: String): Result<Unit> =
        withContext(dispatcherProvider.io) {
            blacklistsDataSource.addBlacklistedDomain(domain)
        }

    override suspend fun removeBlacklistedDomain(domain: String): Result<Unit> =
        withContext(dispatcherProvider.io) {
            blacklistsDataSource.removeBlacklistedDomain(domain)
        }

    private fun PaginationDto.toPaginationWithResolvedNextPage(
        currentPage: Int,
    ): Pagination {
        val pagination = toPagination()
        if (pagination.next.isNotBlank()) {
            return pagination
        }

        if (pagination.perPage <= 0 || pagination.total <= pagination.perPage * currentPage) {
            return pagination.copy(next = "")
        }

        return pagination.copy(next = (currentPage + 1).toString())
    }
}
