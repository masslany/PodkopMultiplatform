package pl.masslany.podkop.business.auth.data.api

import pl.masslany.podkop.business.auth.data.network.models.AuthDto
import pl.masslany.podkop.business.auth.data.network.models.WykopConnectDto

interface AuthDataSource {
    suspend fun getAuthToken(): Result<AuthDto>

    suspend fun getWykopConnect(): Result<WykopConnectDto>

    suspend fun shouldUpdateTokens(): Boolean

    suspend fun updateTokens(): Result<Unit>

    suspend fun logout(): Result<Unit>
}
