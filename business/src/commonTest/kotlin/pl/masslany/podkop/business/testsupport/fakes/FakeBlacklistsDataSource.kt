package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.blacklists.data.api.BlacklistsDataSource
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedDomainsResponseDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedTagsResponseDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistedUsersResponseDto

class FakeBlacklistsDataSource : BlacklistsDataSource {
    var getBlacklistedUsersResult: Result<BlacklistedUsersResponseDto> =
        unstubbedResult("BlacklistsDataSource.getBlacklistedUsers")
    var getBlacklistedTagsResult: Result<BlacklistedTagsResponseDto> =
        unstubbedResult("BlacklistsDataSource.getBlacklistedTags")
    var getBlacklistedDomainsResult: Result<BlacklistedDomainsResponseDto> =
        unstubbedResult("BlacklistsDataSource.getBlacklistedDomains")
    var addBlacklistedUserResult: Result<Unit> = unstubbedResult("BlacklistsDataSource.addBlacklistedUser")
    var removeBlacklistedUserResult: Result<Unit> = unstubbedResult("BlacklistsDataSource.removeBlacklistedUser")
    var addBlacklistedTagResult: Result<Unit> = unstubbedResult("BlacklistsDataSource.addBlacklistedTag")
    var removeBlacklistedTagResult: Result<Unit> = unstubbedResult("BlacklistsDataSource.removeBlacklistedTag")
    var addBlacklistedDomainResult: Result<Unit> = unstubbedResult("BlacklistsDataSource.addBlacklistedDomain")
    var removeBlacklistedDomainResult: Result<Unit> =
        unstubbedResult("BlacklistsDataSource.removeBlacklistedDomain")

    val getBlacklistedUsersCalls = mutableListOf<Int>()
    val getBlacklistedTagsCalls = mutableListOf<Int>()
    val getBlacklistedDomainsCalls = mutableListOf<Int>()
    val addBlacklistedUserCalls = mutableListOf<String>()
    val removeBlacklistedUserCalls = mutableListOf<String>()
    val addBlacklistedTagCalls = mutableListOf<String>()
    val removeBlacklistedTagCalls = mutableListOf<String>()
    val addBlacklistedDomainCalls = mutableListOf<String>()
    val removeBlacklistedDomainCalls = mutableListOf<String>()

    override suspend fun getBlacklistedUsers(page: Int): Result<BlacklistedUsersResponseDto> {
        getBlacklistedUsersCalls += page
        return getBlacklistedUsersResult
    }

    override suspend fun getBlacklistedTags(page: Int): Result<BlacklistedTagsResponseDto> {
        getBlacklistedTagsCalls += page
        return getBlacklistedTagsResult
    }

    override suspend fun getBlacklistedDomains(page: Int): Result<BlacklistedDomainsResponseDto> {
        getBlacklistedDomainsCalls += page
        return getBlacklistedDomainsResult
    }

    override suspend fun addBlacklistedUser(username: String): Result<Unit> {
        addBlacklistedUserCalls += username
        return addBlacklistedUserResult
    }

    override suspend fun removeBlacklistedUser(username: String): Result<Unit> {
        removeBlacklistedUserCalls += username
        return removeBlacklistedUserResult
    }

    override suspend fun addBlacklistedTag(tag: String): Result<Unit> {
        addBlacklistedTagCalls += tag
        return addBlacklistedTagResult
    }

    override suspend fun removeBlacklistedTag(tag: String): Result<Unit> {
        removeBlacklistedTagCalls += tag
        return removeBlacklistedTagResult
    }

    override suspend fun addBlacklistedDomain(domain: String): Result<Unit> {
        addBlacklistedDomainCalls += domain
        return addBlacklistedDomainResult
    }

    override suspend fun removeBlacklistedDomain(domain: String): Result<Unit> {
        removeBlacklistedDomainCalls += domain
        return removeBlacklistedDomainResult
    }
}
