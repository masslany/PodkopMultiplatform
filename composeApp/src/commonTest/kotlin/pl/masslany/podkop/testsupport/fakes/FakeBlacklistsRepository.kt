package pl.masslany.podkop.testsupport.fakes

import pl.masslany.podkop.business.blacklists.domain.main.BlacklistsRepository
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedDomains
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedTags
import pl.masslany.podkop.business.blacklists.domain.models.BlacklistedUsers

class FakeBlacklistsRepository : BlacklistsRepository {
    var getBlacklistedUsersHandler: suspend (page: Int) -> Result<BlacklistedUsers> =
        { error("FakeBlacklistsRepository.getBlacklistedUsers not stubbed") }
    var getBlacklistedTagsHandler: suspend (page: Int) -> Result<BlacklistedTags> =
        { error("FakeBlacklistsRepository.getBlacklistedTags not stubbed") }
    var getBlacklistedDomainsHandler: suspend (page: Int) -> Result<BlacklistedDomains> =
        { error("FakeBlacklistsRepository.getBlacklistedDomains not stubbed") }
    var addBlacklistedUserResult: Result<Unit> = Result.success(Unit)
    var removeBlacklistedUserResult: Result<Unit> = Result.success(Unit)
    var addBlacklistedTagResult: Result<Unit> = Result.success(Unit)
    var removeBlacklistedTagResult: Result<Unit> = Result.success(Unit)
    var addBlacklistedDomainResult: Result<Unit> = Result.success(Unit)
    var removeBlacklistedDomainResult: Result<Unit> = Result.success(Unit)

    val getBlacklistedUsersCalls = mutableListOf<Int>()
    val getBlacklistedTagsCalls = mutableListOf<Int>()
    val getBlacklistedDomainsCalls = mutableListOf<Int>()
    val addBlacklistedUserCalls = mutableListOf<String>()
    val removeBlacklistedUserCalls = mutableListOf<String>()
    val addBlacklistedTagCalls = mutableListOf<String>()
    val removeBlacklistedTagCalls = mutableListOf<String>()
    val addBlacklistedDomainCalls = mutableListOf<String>()
    val removeBlacklistedDomainCalls = mutableListOf<String>()

    override suspend fun getBlacklistedUsers(page: Int): Result<BlacklistedUsers> {
        getBlacklistedUsersCalls += page
        return getBlacklistedUsersHandler(page)
    }

    override suspend fun getBlacklistedTags(page: Int): Result<BlacklistedTags> {
        getBlacklistedTagsCalls += page
        return getBlacklistedTagsHandler(page)
    }

    override suspend fun getBlacklistedDomains(page: Int): Result<BlacklistedDomains> {
        getBlacklistedDomainsCalls += page
        return getBlacklistedDomainsHandler(page)
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

    companion object {
        fun withEmptyPages() = FakeBlacklistsRepository().apply {
            getBlacklistedUsersHandler = { Result.success(BlacklistedUsers(data = emptyList(), pagination = null)) }
            getBlacklistedTagsHandler = { Result.success(BlacklistedTags(data = emptyList(), pagination = null)) }
            getBlacklistedDomainsHandler = { Result.success(BlacklistedDomains(data = emptyList(), pagination = null)) }
        }
    }
}
