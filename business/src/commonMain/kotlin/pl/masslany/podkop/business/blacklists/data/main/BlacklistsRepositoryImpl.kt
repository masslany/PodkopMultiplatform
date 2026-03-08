package pl.masslany.podkop.business.blacklists.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.blacklists.data.api.BlacklistsDataSource
import pl.masslany.podkop.business.blacklists.domain.main.BlacklistsRepository
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class BlacklistsRepositoryImpl(
    private val blacklistsDataSource: BlacklistsDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : BlacklistsRepository {
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
}
