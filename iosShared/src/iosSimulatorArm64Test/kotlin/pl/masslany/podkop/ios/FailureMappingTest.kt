package pl.masslany.podkop.ios

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.common.network.api.HttpStatusFailure

class FailureMappingTest {
    private class StatusFailure(override val statusCode: Int) : HttpStatusFailure, Exception("HTTP $statusCode")

    @Test
    fun apiClientHttpFailuresKeepTheirCategoryAndCode() {
        val cases = mapOf(
            400 to "validation", 401 to "unauthorized", 403 to "forbidden",
            404 to "notFound", 429 to "rateLimited", 503 to "server", 418 to "unknown",
        )
        cases.forEach { (status, category) ->
            val failure = StatusFailure(status).toIOSFailure()
            assertEquals(category, failure.category, "status $status")
            assertEquals(status.toString(), failure.code)
        }
    }

    @Test
    fun otherFailuresStayUnknownWithoutLeakingMessages() {
        val failure = IllegalStateException("token=secret").toIOSFailure()
        assertEquals("unknown", failure.category)
        assertEquals(null, failure.code)
    }
}
