package pl.masslany.podkop.test.common

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource
import pl.masslany.podkop.test.TestMainApplication
import pl.masslany.podkop.test.support.MockApiServer

/**
 * The orchestrator gives every test a fresh app process with cleared data, so the app, Koin and the
 * mock server all start clean; this rule only registers the test's routes before the activity starts.
 */
class IntegrationTestRule(
    private val configureMockApi: (MockApiServer) -> Unit,
) : ExternalResource() {
    val mockApiServer: MockApiServer
        get() = testApplication.mockApiServer

    override fun before() {
        configureMockApi(mockApiServer)
    }
}

private val testApplication: TestMainApplication
    get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestMainApplication
