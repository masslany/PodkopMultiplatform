package pl.masslany.podkop.test.common

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import pl.masslany.podkop.test.TestMainApplication
import pl.masslany.podkop.test.support.MockApiServer

/**
 * The orchestrator gives every test a fresh app process with cleared data, so the app, Koin and the
 * mock server all start clean; this rule registers the test's routes before the activity starts and
 * fails the test if the app made any request those routes don't cover.
 */
class IntegrationTestRule(
    private val configureMockApi: (MockApiServer) -> Unit,
) : TestRule {
    val mockApiServer: MockApiServer
        get() = testApplication.mockApiServer

    override fun apply(
        base: Statement,
        description: Description,
    ): Statement =
        object : Statement() {
            override fun evaluate() {
                configureMockApi(mockApiServer)
                try {
                    base.evaluate()
                } catch (failure: Throwable) {
                    // A missing route usually explains the failure (often a timeout), so it leads the
                    // report: test results keep only one failure per test.
                    mockApiServer.assertAllRequestsMatched(cause = failure)
                    throw failure
                }
                mockApiServer.assertAllRequestsMatched()
            }
        }
}

private val testApplication: TestMainApplication
    get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestMainApplication
