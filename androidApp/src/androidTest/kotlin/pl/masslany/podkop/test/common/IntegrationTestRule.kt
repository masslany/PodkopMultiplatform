package pl.masslany.podkop.test.common

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.koin.core.context.GlobalContext
import pl.masslany.podkop.business.auth.domain.AuthRepository
import pl.masslany.podkop.test.TestMainApplication
import pl.masslany.podkop.test.support.AuthRoutes
import pl.masslany.podkop.test.support.AuthRoutes.signedInSession
import pl.masslany.podkop.test.support.MockApiServer

/**
 * The orchestrator gives every test a fresh app process with cleared data, so the app, Koin and the
 * mock server all start clean. Before the activity starts, this rule signs the app in if the test
 * asks for it and registers the test's routes; it fails the test if the app made any request those
 * routes don't cover.
 */
class IntegrationTestRule(
    private val configureMockApi: (MockApiServer) -> Unit,
    private val isSignedIn: () -> Boolean,
) : TestRule {
    val mockApiServer: MockApiServer
        get() = testApplication.mockApiServer

    override fun apply(
        base: Statement,
        description: Description,
    ): Statement =
        object : Statement() {
            override fun evaluate() {
                if (isSignedIn()) {
                    signIn()
                }
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

    private fun signIn() {
        mockApiServer.signedInSession()
        runBlocking {
            GlobalContext.get().get<AuthRepository>().storeSessionTokens(
                token = AuthRoutes.APP_TOKEN,
                refreshToken = AuthRoutes.REFRESH_TOKEN,
            )
        }
    }
}

private val testApplication: TestMainApplication
    get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestMainApplication
