package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.blacklists.data.api.BlacklistsDataSource

class FakeBlacklistsDataSource : BlacklistsDataSource {
    var addBlacklistedUserResult: Result<Unit> = unstubbedResult("BlacklistsDataSource.addBlacklistedUser")
    var removeBlacklistedUserResult: Result<Unit> = unstubbedResult("BlacklistsDataSource.removeBlacklistedUser")
    var addBlacklistedTagResult: Result<Unit> = unstubbedResult("BlacklistsDataSource.addBlacklistedTag")
    var removeBlacklistedTagResult: Result<Unit> = unstubbedResult("BlacklistsDataSource.removeBlacklistedTag")

    val addBlacklistedUserCalls = mutableListOf<String>()
    val removeBlacklistedUserCalls = mutableListOf<String>()
    val addBlacklistedTagCalls = mutableListOf<String>()
    val removeBlacklistedTagCalls = mutableListOf<String>()

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
}
