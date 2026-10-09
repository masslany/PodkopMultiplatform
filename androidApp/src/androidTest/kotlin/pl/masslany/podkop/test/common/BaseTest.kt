package pl.masslany.podkop.test.common

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import pl.masslany.podkop.MainActivity
import pl.masslany.podkop.test.support.MockApiServer

abstract class BaseTest {
    @get:Rule(order = 0)
    val integrationRule = IntegrationTestRule(
        configureMockApi = ::configureMockApi,
        isSignedIn = { signedIn },
    )

    @get:Rule(order = 1)
    val activityRule = createAndroidComposeRule<MainActivity>()

    protected val mockApiServer: MockApiServer
        get() = integrationRule.mockApiServer

    /** Whether the app starts signed in, with a session the mock API accepts. */
    protected open val signedIn: Boolean = false

    protected open fun configureMockApi(mockApiServer: MockApiServer) = Unit

    /**
     * Waits for the app to send [method] [path], for side effects like marking something read.
     * It waits through the compose rule, so the UI keeps running meanwhile.
     */
    protected fun awaitRequest(
        method: String,
        path: String,
        timeoutMillis: Long = 5_000,
    ) {
        try {
            activityRule.waitUntil(timeoutMillis) { mockApiServer.hasRequested(method, path) }
        } catch (timeout: ComposeTimeoutException) {
            throw AssertionError("Expected $method $path. The app requested:\n" + mockApiServer.requestLog(), timeout)
        }
    }
}

typealias PodkopComposeRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
