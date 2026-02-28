package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.auth.data.network.api.AuthApi
import pl.masslany.podkop.business.auth.data.network.models.AuthDto
import pl.masslany.podkop.business.auth.data.network.models.RefreshDto
import pl.masslany.podkop.business.auth.data.network.models.WykopConnectDto

class FakeAuthApi : AuthApi {
    var getAuthTokenResult: Result<AuthDto> = unstubbedResult("AuthApi.getAuthToken")
    var getWykopConnectResult: Result<WykopConnectDto> = unstubbedResult("AuthApi.getWykopConnect")
    var refreshTokensResult: Result<RefreshDto> = unstubbedResult("AuthApi.refreshTokens")
    var logoutResult: Result<Unit> = unstubbedResult("AuthApi.logout")

    val getAuthTokenCalls = mutableListOf<Pair<String, String>>()
    val refreshTokensCalls = mutableListOf<String>()

    override suspend fun getAuthToken(
        key: String,
        secret: String,
    ): Result<AuthDto> {
        getAuthTokenCalls += key to secret
        return getAuthTokenResult
    }

    override suspend fun getWykopConnect(): Result<WykopConnectDto> = getWykopConnectResult

    override suspend fun refreshTokens(refreshToken: String): Result<RefreshDto> {
        refreshTokensCalls += refreshToken
        return refreshTokensResult
    }

    override suspend fun logout(): Result<Unit> = logoutResult
}
