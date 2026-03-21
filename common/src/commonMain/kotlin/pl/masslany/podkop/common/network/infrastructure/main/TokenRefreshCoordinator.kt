package pl.masslany.podkop.common.network.infrastructure.main

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import pl.masslany.podkop.common.configstorage.api.ConfigStorage
import pl.masslany.podkop.common.logging.api.AppLogger

internal class TokenRefreshCoordinator(
    private val configStorage: ConfigStorage,
    private val json: Json,
    private val logger: AppLogger,
    private val refreshHttpClient: HttpClient = createRefreshHttpClient(json),
) : TokenRefreshHandler {
    private val refreshMutex = Mutex()

    override suspend fun refreshIfTokenExpiring(): Boolean = refreshIfTokenExpiring(TOKEN_EXPIRATION_LEEWAY_SECONDS)

    suspend fun refreshIfTokenExpiring(leewaySeconds: Long = TOKEN_EXPIRATION_LEEWAY_SECONDS): Boolean {
        val token = configStorage.getBearerToken()
        if (token.isEmpty()) {
            return refreshTokens(force = true)
        }

        return if (token.isExpiringIn(leewaySeconds, json)) {
            refreshTokens(force = false)
        } else {
            true
        }
    }

    override suspend fun refreshTokens(force: Boolean): Boolean {
        return refreshMutex.withLock {
            if (!force) {
                val currentToken = configStorage.getBearerToken()
                if (currentToken.isNotEmpty() && !currentToken.isExpiringIn(TOKEN_EXPIRATION_LEEWAY_SECONDS, json)) {
                    return@withLock true
                }
            }

            val refreshToken = configStorage.getRefreshToken()
            if (refreshToken.isNotEmpty() && refreshWithRefreshToken(refreshToken)) {
                return@withLock true
            }

            return@withLock refreshWithApiCredentials()
        }
    }

    private suspend fun refreshWithRefreshToken(refreshToken: String): Boolean {
        return runCatching {
            val response =
                refreshHttpClient.post("$BASE_URL$REFRESH_TOKEN_PATH") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(
                        RefreshRequestDto(
                            data = RefreshRequestData(refreshToken = refreshToken),
                        ),
                    )
                }.body<RefreshDto>()

            configStorage.storeBearerToken(response.data.token)
            configStorage.storeRefreshToken(
                response.data.refreshToken?.takeIf(String::isNotBlank) ?: refreshToken,
            )
            true
        }
            .onFailure {
                logger.warn("Failed to refresh API token with refresh token", it)
            }
            .getOrDefault(false)
    }

    private suspend fun refreshWithApiCredentials(): Boolean {
        val key = configStorage.getApiKey()
        val secret = configStorage.getApiSecret()
        if (key.isEmpty() || secret.isEmpty()) {
            return false
        }

        return runCatching {
            val response =
                refreshHttpClient.post("$BASE_URL$AUTH_PATH") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    setBody(
                        AuthRequestDto(
                            data = AuthRequestData(key = key, secret = secret),
                        ),
                    )
                }.body<AuthDto>()

            configStorage.storeBearerToken(response.data.token)
            true
        }
            .onFailure {
                logger.warn("Failed to refresh API token with API credentials", it)
            }
            .getOrDefault(false)
    }

    private companion object {
        const val BASE_URL = "https://wykop.pl/"
        const val AUTH_PATH = "api/v3/auth"
        const val REFRESH_TOKEN_PATH = "api/v3/refresh-token"
        const val TOKEN_EXPIRATION_LEEWAY_SECONDS = 60L
    }
}

@Serializable
private data class RefreshRequestDto(
    @SerialName("data")
    val data: RefreshRequestData,
)

@Serializable
private data class RefreshRequestData(
    @SerialName("refresh_token")
    val refreshToken: String,
)

@Serializable
private data class AuthRequestDto(
    @SerialName("data")
    val data: AuthRequestData,
)

@Serializable
private data class AuthRequestData(
    @SerialName("key")
    val key: String,
    @SerialName("secret")
    val secret: String,
)

@Serializable
private data class AuthDto(
    @SerialName("data")
    val data: AuthDtoData,
)

@Serializable
private data class AuthDtoData(
    @SerialName("token")
    val token: String,
)

@Serializable
private data class RefreshDto(
    @SerialName("data")
    val data: RefreshDtoData,
)

@Serializable
private data class RefreshDtoData(
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("token")
    val token: String,
)

private fun createRefreshHttpClient(json: Json): HttpClient {
    return HttpClient(HttpClientEngineProvider.provide()) {
        install(ContentNegotiation) {
            json(json)
        }
    }
}
