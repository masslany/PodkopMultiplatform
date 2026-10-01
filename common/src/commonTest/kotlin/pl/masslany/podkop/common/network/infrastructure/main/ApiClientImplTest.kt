package pl.masslany.podkop.common.network.infrastructure.main

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request
import pl.masslany.podkop.common.network.models.response.ApiResponse
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ApiClientImplTest {

    @Test
    fun `cancelling a request cancels the caller instead of returning a failure`() = runBlocking {
        val requestStarted = CompletableDeferred<Unit>()
        val sut = ApiClientImpl(
            testHttpClient {
                requestStarted.complete(Unit)
                awaitCancellation()
            },
        )
        var result: Result<ApiResponse<String>>? = null
        var reachedCodeAfterRequest = false

        val job = launch {
            result = sut.request(Request<String>(method = Request.HttpMethod.GET, path = "api/v3/links"))
            reachedCodeAfterRequest = true
        }
        requestStarted.await()
        job.cancelAndJoin()

        assertNull(result)
        assertTrue(!reachedCodeAfterRequest)
    }

    @Test
    fun `request errors still come back as a failure`() = runBlocking<Unit> {
        val sut = ApiClientImpl(testHttpClient { error("connection reset") })

        val result = sut.request(Request<String>(method = Request.HttpMethod.GET, path = "api/v3/links"))

        assertIs<IllegalStateException>(result.exceptionOrNull())
    }
}
