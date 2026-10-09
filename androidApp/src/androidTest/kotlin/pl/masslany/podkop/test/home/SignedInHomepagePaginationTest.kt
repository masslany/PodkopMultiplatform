package pl.masslany.podkop.test.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pl.masslany.podkop.test.common.BaseTest
import pl.masslany.podkop.test.fixtures.LinkFixtures
import pl.masslany.podkop.test.home.HomepageRoutes.homepageSignedIn
import pl.masslany.podkop.test.home.robots.homepage
import pl.masslany.podkop.test.support.MockApiServer

@RunWith(AndroidJUnit4::class)
class SignedInHomepagePaginationTest : BaseTest() {
    override val signedIn = true

    override fun configureMockApi(mockApiServer: MockApiServer) {
        mockApiServer.homepageSignedIn()
    }

    @Test
    fun signedInHomepageFollowsTheNextCursorAfterScrolling() {
        homepage(activityRule) {
            displayHomepageList()
            displayTitle(LinkFixtures.linkTitle(page = 1, index = 1))

            scrollToLink(LinkFixtures.linkId(page = 1, index = LinkFixtures.SIGNED_IN_LINKS_PER_PAGE))
            scrollToLink(LinkFixtures.linkId(page = 2, index = LinkFixtures.SIGNED_IN_LINKS_PER_PAGE))
            displayTitle(LinkFixtures.linkTitle(page = 2, index = LinkFixtures.SIGNED_IN_LINKS_PER_PAGE))
        }
    }
}
