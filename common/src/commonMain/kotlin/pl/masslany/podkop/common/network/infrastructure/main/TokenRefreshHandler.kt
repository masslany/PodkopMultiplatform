package pl.masslany.podkop.common.network.infrastructure.main

internal interface TokenRefreshHandler {
    suspend fun refreshIfTokenExpiring(): Boolean

    suspend fun refreshTokens(force: Boolean): Boolean
}
