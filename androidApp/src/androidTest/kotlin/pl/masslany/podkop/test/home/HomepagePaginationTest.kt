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
class HomepagePaginationTest : BaseTest() {
    override fun configureMockApi(mockApiServer: MockApiServer) {
        mockApiServer.homepageLoggedOut()
    }

    @Test
    fun loggedOutHomepageLoadsNextPageAfterScrolling() {
        homepage(activityRule) {
            displayHomepageList()
            displayTitle(LinkFixtures.linkTitle(page = 1, index = 1))

            scrollToLink(LinkFixtures.linkId(page = 1, index = LINKS_PER_PAGE))
            scrollToLink(LinkFixtures.linkId(page = 2, index = LINKS_PER_PAGE))
            displayTitle(LinkFixtures.linkTitle(page = 2, index = LINKS_PER_PAGE))
        }
    }

    private companion object {
        const val LINKS_PER_PAGE = 25
    }
}
