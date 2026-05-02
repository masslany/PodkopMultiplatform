package pl.masslany.podkop.test.support

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.CopyOnWriteArrayList
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.HttpUrl

class MockApiServer {
    private val context: Context =
        InstrumentationRegistry.getInstrumentation().context
    private val server = MockWebServer()
    private val routes = mutableListOf<Route>()
    private val requests = CopyOnWriteArrayList<RecordedRequest>()

    fun start(): String {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                requests += request
                val url = request.url

                val route = routes.firstOrNull { it.matches(request.method, url) }
                return if (route != null) {
                    jsonAssetResponse(route.assetPath)
                } else {
                    jsonResponse(
                        code = 404,
                        body = """{"error":"No route for ${request.method} ${url.encodedPath}"}""",
                    )
                }
            }
        }
        server.start()
        return server.url("/").toString()
    }

    fun getJson(
        path: String,
        assetPath: String,
        query: Map<String, String> = emptyMap(),
    ) {
        routes += Route(
            method = "GET",
            path = path,
            query = query,
            assetPath = assetPath,
        )
    }

    fun assertRequested(
        path: String,
        query: Map<String, String>,
    ) {
        check(hasRequested(path = path, query = query)) {
            val requestList = requests.joinToString(separator = "\n") { request ->
                "${request.method} ${request.url}"
            }
            "Expected GET $path with $query. Recorded requests:\n$requestList"
        }
    }

    fun hasRequested(
        path: String,
        query: Map<String, String>,
    ): Boolean = requests.any { request ->
        val url = request.url
        request.method == "GET" &&
            url.encodedPath == path &&
            query.all { (name, value) -> url.queryParameter(name) == value }
    }

    fun shutdown() {
        server.close()
        routes.clear()
        requests.clear()
    }

    private fun jsonAssetResponse(assetPath: String): MockResponse =
        jsonResponse(body = readAsset(assetPath))

    private fun jsonResponse(
        code: Int = 200,
        body: String,
    ): MockResponse =
        MockResponse.Builder()
            .code(code)
            .setHeader("Content-Type", "application/json")
            .body(body)
            .build()

    private fun readAsset(path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }
}

private data class Route(
    val method: String,
    val path: String,
    val query: Map<String, String>,
    val assetPath: String,
) {
    fun matches(
        requestMethod: String?,
        url: HttpUrl,
    ): Boolean =
        requestMethod == method &&
            url.encodedPath == path &&
            url.queryParameterNames == query.keys &&
            query.all { (name, value) -> url.queryParameter(name) == value }
}
