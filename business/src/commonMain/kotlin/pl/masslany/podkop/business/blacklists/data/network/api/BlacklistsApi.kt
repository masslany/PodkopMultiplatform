package pl.masslany.podkop.business.blacklists.data.network.api

import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedDomainsResponseDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedTagsResponseDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedUsersResponseDto

interface BlacklistsApi {
    suspend fun getBlacklistedUsers(page: Int): Result<BlacklistedUsersResponseDto>

    suspend fun getBlacklistedTags(page: Int): Result<BlacklistedTagsResponseDto>

    suspend fun getBlacklistedDomains(page: Int): Result<BlacklistedDomainsResponseDto>

    suspend fun addBlacklistedUser(username: String): Result<Unit>

    suspend fun removeBlacklistedUser(username: String): Result<Unit>

    suspend fun addBlacklistedTag(tag: String): Result<Unit>

    suspend fun removeBlacklistedTag(tag: String): Result<Unit>

    suspend fun addBlacklistedDomain(domain: String): Result<Unit>

    suspend fun removeBlacklistedDomain(domain: String): Result<Unit>
}
