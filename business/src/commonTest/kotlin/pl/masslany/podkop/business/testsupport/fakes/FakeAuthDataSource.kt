package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.auth.data.api.AuthDataSource
import pl.masslany.podkop.business.auth.data.network.models.AuthDto
import pl.masslany.podkop.business.auth.data.network.models.WykopConnectDto

class FakeAuthDataSource : AuthDataSource {
    data class StoreSessionTokensCall(
        val token: String,
        val refreshToken: String,
    )

    var isLoggedInValue: Boolean = false
    var getAuthTokenResult: Result<AuthDto> = unstubbedResult("AuthDataSource.getAuthToken")
    var getWykopConnectResult: Result<WykopConnectDto> = unstubbedResult("AuthDataSource.getWykopConnect")
    var shouldUpdateTokensValue: Boolean = false
    var updateTokensResult: Result<Unit> = unstubbedResult("AuthDataSource.updateTokens")
    var logoutResult: Result<Unit> = unstubbedResult("AuthDataSource.logout")

    var isLoggedInCalls = 0
    var getAuthTokenCalls = 0
    var getWykopConnectCalls = 0
    val storeSessionTokensCalls = mutableListOf<StoreSessionTokensCall>()
    var shouldUpdateTokensCalls = 0
    var updateTokensCalls = 0
    var logoutCalls = 0

    override suspend fun isLoggedIn(): Boolean {
        isLoggedInCalls += 1
        return isLoggedInValue
    }

    override suspend fun getAuthToken(): Result<AuthDto> {
        getAuthTokenCalls += 1
        return getAuthTokenResult
    }

    override suspend fun getWykopConnect(): Result<WykopConnectDto> {
        getWykopConnectCalls += 1
        return getWykopConnectResult
    }

    override suspend fun storeSessionTokens(
        token: String,
        refreshToken: String,
    ) {
        storeSessionTokensCalls += StoreSessionTokensCall(token = token, refreshToken = refreshToken)
    }

    override suspend fun shouldUpdateTokens(): Boolean {
        shouldUpdateTokensCalls += 1
        return shouldUpdateTokensValue
    }

    override suspend fun updateTokens(): Result<Unit> {
        updateTokensCalls += 1
        return updateTokensResult
    }

    override suspend fun logout(): Result<Unit> {
        logoutCalls += 1
        return logoutResult
    }
}
