package pl.masslany.podkop.business.blacklists.data.network.main

import pl.masslany.podkop.business.blacklists.data.api.BlacklistsDataSource
import pl.masslany.podkop.business.blacklists.data.network.api.BlacklistsApi

class BlacklistsDataSourceImpl(
    private val blacklistsApi: BlacklistsApi,
) : BlacklistsDataSource {
    override suspend fun getBlacklistedUsers(page: Int) = blacklistsApi.getBlacklistedUsers(page = page)

    override suspend fun getBlacklistedTags(page: Int) = blacklistsApi.getBlacklistedTags(page = page)

    override suspend fun getBlacklistedDomains(page: Int) = blacklistsApi.getBlacklistedDomains(page = page)

    override suspend fun addBlacklistedUser(username: String): Result<Unit> {
        return blacklistsApi.addBlacklistedUser(username)
    }

    override suspend fun removeBlacklistedUser(username: String): Result<Unit> {
        return blacklistsApi.removeBlacklistedUser(username)
    }

    override suspend fun addBlacklistedTag(tag: String): Result<Unit> {
        return blacklistsApi.addBlacklistedTag(tag)
    }

    override suspend fun removeBlacklistedTag(tag: String): Result<Unit> {
        return blacklistsApi.removeBlacklistedTag(tag)
    }

    override suspend fun addBlacklistedDomain(domain: String): Result<Unit> {
        return blacklistsApi.addBlacklistedDomain(domain)
    }

    override suspend fun removeBlacklistedDomain(domain: String): Result<Unit> {
        return blacklistsApi.removeBlacklistedDomain(domain)
    }
}
