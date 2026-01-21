package pl.masslany.podkop.business.auth.data.network.api

import pl.masslany.podkop.business.auth.data.network.models.AuthDto
import pl.masslany.podkop.business.auth.data.network.models.RefreshDto
import pl.masslany.podkop.business.auth.data.network.models.WykopConnectDto

internal interface AuthApi {
    suspend fun getAuthToken(
        key: String,
        secret: String,
    ): Result<AuthDto>

    suspend fun getWykopConnect(): Result<WykopConnectDto>

    suspend fun refreshTokens(refreshToken: String): Result<RefreshDto>

    suspend fun logout(): Result<Unit>
}
