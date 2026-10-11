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
class HomepageNextPageErrorTest : BaseTest() {
    override fun configureMockApi(mockApiServer: MockApiServer) {
        mockApiServer.homepageLoggedOut()
        mockApiServer.failOnce(
            method = "GET",
            path = HomepageRoutes.LINKS_PATH,
            query = mapOf("sort" to "newest", "type" to "homepage", "page" to "2"),
        )
    }

    @Test
    fun aNextPageThatFailedToLoadLoadsOnRetry() {
        homepage(activityRule) {
            scrollToLink(LinkFixtures.linkId(page = 1, index = GUEST_LINKS_PER_PAGE))
            // Scrolling does not ask again for a failed page; the button below the list does.
            retryNextPage()
            scrollToLink(LinkFixtures.linkId(page = 2, index = 1))
            displayTitle(LinkFixtures.linkTitle(page = 2, index = 1))
        }
    }

    private companion object {
        const val GUEST_LINKS_PER_PAGE = 25
    }
}
