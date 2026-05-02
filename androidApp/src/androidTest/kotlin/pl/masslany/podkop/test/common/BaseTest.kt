package pl.masslany.podkop.test.common

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
        configureEnvironment = ::configureEnvironment,
    )

    @get:Rule(order = 1)
    val activityRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val disableAnimationsRule = DisableAnimationsRule()

    protected val mockApiServer: MockApiServer
        get() = integrationRule.mockApiServer

    protected open fun configureMockApi(mockApiServer: MockApiServer) = Unit

    protected open fun configureEnvironment() = Unit
}

typealias PodkopComposeRule = AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
