package pl.masslany.podkop.test.support

import android.content.res.AssetManager
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.HttpUrl

class MockApiServer(
    private val assets: AssetManager,
) {
    private val server = MockWebServer()
    private val routes = CopyOnWriteArrayList<Route>()
    private val unmatchedRequests = CopyOnWriteArrayList<String>()
    private val requestLines = CopyOnWriteArrayList<String>()

    lateinit var baseUrl: String
        private set

    fun start() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val url = request.url
                val requestLine = "${request.method} ${url.encodedPath}${url.encodedQuery?.let { "?$it" }.orEmpty()}"
                requestLines += requestLine

                val route = routes.firstOrNull { it.matches(request.method, url) && it.take() }
                return if (route != null) {
                    response(code = route.code, body = route.body, contentType = route.contentType)
                } else {
                    unmatchedRequests += requestLine
                    response(
                        code = 404,
                        body = """{"error":"No route for $requestLine"}""",
                    )
                }
            }
        }
        // Application.onCreate runs on the main thread, where Android forbids the localhost lookup made here.
        baseUrl = CompletableFuture.supplyAsync {
            server.start()
            server.url("/").toString()
        }.get()
    }

    fun getJson(
        path: String,
        body: String,
        query: Map<String, String> = emptyMap(),
    ) {
        routes += Route(
            method = "GET",
            path = path,
            query = query,
            body = body,
        )
    }

    /**
     * Answers the next [method] [path] request with [code] before its usual route, e.g. to show an
     * error the user then retries. Any non-2xx status fails a request; the body is not read.
     */
    fun failOnce(
        method: String,
        path: String,
        query: Map<String, String> = emptyMap(),
        code: Int = 503,
    ) {
        routes.add(
            0,
            Route(
                method = method,
                path = path,
                query = query,
                body = """{"error":"Injected $code"}""",
                code = code,
                uses = AtomicInteger(1),
            ),
        )
    }

    /** Serves a non-JSON file, e.g. an image the app loads from a URL in a response. */
    fun get(
        path: String,
        body: String,
        contentType: String,
    ) {
        routes += Route(
            method = "GET",
            path = path,
            query = emptyMap(),
            body = body,
            contentType = contentType,
        )
    }

    /** Answers an update the app sends, e.g. marking a notification read, which the API answers with 204. */
    fun put(
        path: String,
        code: Int = 204,
    ) {
        routes += Route(
            method = "PUT",
            path = path,
            query = emptyMap(),
            body = "",
            code = code,
        )
    }

    fun hasRequested(
        method: String,
        path: String,
    ): Boolean = requestLines.any { it.substringBefore('?') == "$method $path" }

    fun requestLog(): String = requestLines.joinToString(separator = "\n")

    fun postJson(
        path: String,
        body: String,
    ) {
        routes += Route(
            method = "POST",
            path = path,
            query = emptyMap(),
            body = body,
        )
    }

    /** A sanitized sample of a real API response, from `assets/api-samples` (see `scripts/api-samples`). */
    fun sample(name: String): JsonObject =
        Json.parseToJsonElement(readAsset("api-samples/$name.json")).jsonObject

    fun assertAllRequestsMatched(cause: Throwable? = null) {
        if (unmatchedRequests.isNotEmpty()) {
            throw AssertionError(
                "The app made requests without a mocked route:\n" + unmatchedRequests.joinToString(separator = "\n"),
                cause,
            )
        }
    }

    private fun response(
        code: Int = 200,
        body: String,
        contentType: String = JSON,
    ): MockResponse =
        MockResponse.Builder()
            .code(code)
            .setHeader("Content-Type", contentType)
            .body(body)
            .build()

    private fun readAsset(path: String): String =
        assets.open(path).bufferedReader().use { it.readText() }
}

private class Route(
    val method: String,
    val path: String,
    val query: Map<String, String>,
    val body: String,
    val contentType: String = JSON,
    val code: Int = 200,
    /** How many more requests this route answers; unlimited when null. */
    private val uses: AtomicInteger? = null,
) {
    fun take(): Boolean = uses == null || uses.getAndUpdate { (it - 1).coerceAtLeast(0) } > 0

    fun matches(
        requestMethod: String?,
        url: HttpUrl,
    ): Boolean =
        requestMethod == method &&
            url.encodedPath == path &&
            url.queryParameterNames == query.keys &&
            query.all { (name, value) -> url.queryParameter(name) == value }
}

private const val JSON = "application/json"
