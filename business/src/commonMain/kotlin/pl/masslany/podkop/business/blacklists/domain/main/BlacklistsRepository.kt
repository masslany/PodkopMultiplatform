package pl.masslany.podkop.business.blacklists.domain.main

import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedDomains
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedTags
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedUsers

interface BlacklistsRepository {
    suspend fun getBlacklistedUsers(page: Int): Result<BlacklistedUsers>

    suspend fun getBlacklistedTags(page: Int): Result<BlacklistedTags>

    suspend fun getBlacklistedDomains(page: Int): Result<BlacklistedDomains>

    suspend fun addBlacklistedUser(username: String): Result<Unit>

    suspend fun removeBlacklistedUser(username: String): Result<Unit>

    suspend fun addBlacklistedTag(tag: String): Result<Unit>

    suspend fun removeBlacklistedTag(tag: String): Result<Unit>

    suspend fun addBlacklistedDomain(domain: String): Result<Unit>

    suspend fun removeBlacklistedDomain(domain: String): Result<Unit>
}
