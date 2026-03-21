package pl.masslany.podkop.common.network.infrastructure.main

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.Json
import pl.masslany.podkop.common.configstorage.api.ConfigStorage
import pl.masslany.podkop.common.logging.api.AppLogger

internal val testJson =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

internal class TestConfigStorage(
    private var apiKey: String = "",
    private var apiSecret: String = "",
    var bearerToken: String = "",
    var refreshToken: String = "",
) : ConfigStorage {
    override suspend fun getApiKey(): String = apiKey

    override suspend fun getApiSecret(): String = apiSecret

    override suspend fun getBearerToken(): String = bearerToken

    override suspend fun getRefreshToken(): String = refreshToken

    override suspend fun storeApiKey(key: String) {
        apiKey = key
    }

    override suspend fun storeApiSecret(secret: String) {
        apiSecret = secret
    }

    override suspend fun storeBearerToken(token: String) {
        bearerToken = token
    }

    override suspend fun storeRefreshToken(refreshToken: String) {
        this.refreshToken = refreshToken
    }
}

internal class RecordingLogger : AppLogger {
    data class Warning(
        val message: String,
        val throwable: Throwable?,
    )

    val warnings = mutableListOf<Warning>()

    override fun debug(message: String) = Unit

    override fun info(message: String) = Unit

    override fun warn(
        message: String,
        throwable: Throwable?,
    ) {
        warnings += Warning(message = message, throwable = throwable)
    }

    override fun error(
        message: String,
        throwable: Throwable?,
    ) = Unit
}

internal fun testHttpClient(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
): HttpClient {
    return HttpClient(MockEngine(handler)) {
        install(ContentNegotiation) {
            json(testJson)
        }
    }
}

internal fun MockRequestHandleScope.respondJson(
    content: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData {
    return respond(
        content = content,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    )
}

internal fun MockRequestHandleScope.respondText(
    content: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData {
    return respond(
        content = content,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString()),
    )
}

@OptIn(ExperimentalEncodingApi::class, ExperimentalTime::class)
internal fun jwtToken(expiresInSeconds: Long): String {
    return jwtTokenAt(Clock.System.now().epochSeconds + expiresInSeconds)
}

@OptIn(ExperimentalEncodingApi::class)
internal fun jwtTokenAt(expirationEpochSeconds: Long): String {
    val header = """{"alg":"HS256","typ":"JWT"}""".encodeJwtPart()
    val payload = """{"exp":$expirationEpochSeconds}""".encodeJwtPart()
    return "$header.$payload.signature"
}

@OptIn(ExperimentalEncodingApi::class)
private fun String.encodeJwtPart(): String {
    return Base64.UrlSafe.encode(encodeToByteArray()).trimEnd('=')
}
