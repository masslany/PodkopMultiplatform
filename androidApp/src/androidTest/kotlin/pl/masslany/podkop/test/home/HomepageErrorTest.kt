package pl.masslany.podkop.test.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pl.masslany.podkop.test.common.BaseTest
import pl.masslany.podkop.test.fixtures.LinkFixtures
import pl.masslany.podkop.test.home.HomepageRoutes.homepageLoggedOut
import pl.masslany.podkop.test.home.robots.homepage
import pl.masslany.podkop.test.support.MockApiServer

@RunWith(AndroidJUnit4::class)
class HomepageErrorTest : BaseTest() {
    override fun configureMockApi(mockApiServer: MockApiServer) {
        mockApiServer.homepageLoggedOut()
        mockApiServer.failOnce(
            method = "GET",
            path = HomepageRoutes.LINKS_PATH,
            query = mapOf("sort" to "newest", "type" to "homepage", "page" to "1"),
        )
    }

    @Test
    fun homepageThatFailedToLoadLoadsOnRetry() {
        homepage(activityRule) {
            displayLoadError()
            retry()
            displayHomepageList()
            displayTitle(LinkFixtures.linkTitle(page = 1, index = 1))
        }
    }
}
