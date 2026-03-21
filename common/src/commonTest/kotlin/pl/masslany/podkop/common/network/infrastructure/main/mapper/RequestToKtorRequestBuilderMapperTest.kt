package pl.masslany.podkop.common.network.infrastructure.main.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.common.network.models.request.Request

class RequestToKtorRequestBuilderMapperTest {

    @Test
    fun `request mapper preserves repeated query parameter pairs`() {
        val request = Request<Unit>(
            method = Request.HttpMethod.GET,
            path = "api/v3/search/stream",
            queryParameters = mapOf(
                "query" to "test",
                "sort" to "score",
            ),
            queryParameterPairs = listOf(
                "users[]" to "wykop",
                "users[]" to "m__b",
                "tags[]" to "android",
                "tags[]" to "ios",
            ),
        )

        val builder = request.toHttpRequestBuilder()

        assertEquals("/api/v3/search/stream", builder.url.encodedPath)
        assertEquals(listOf("test"), builder.url.parameters.getAll("query"))
        assertEquals(listOf("score"), builder.url.parameters.getAll("sort"))
        assertEquals(listOf("wykop", "m__b"), builder.url.parameters.getAll("users[]"))
        assertEquals(listOf("android", "ios"), builder.url.parameters.getAll("tags[]"))
    }
}
