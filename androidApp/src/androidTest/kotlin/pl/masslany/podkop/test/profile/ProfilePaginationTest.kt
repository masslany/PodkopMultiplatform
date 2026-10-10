package pl.masslany.podkop.test.profile

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pl.masslany.podkop.test.common.BaseTest
import pl.masslany.podkop.test.fixtures.LinkFixtures
import pl.masslany.podkop.test.home.HomepageRoutes.homepageLoggedOut
import pl.masslany.podkop.test.profile.ProfileRoutes.profileUsername
import pl.masslany.podkop.test.profile.ProfileRoutes.profileWithShortFirstPage
import pl.masslany.podkop.test.profile.robots.profile
import pl.masslany.podkop.test.support.MockApiServer

@RunWith(AndroidJUnit4::class)
class ProfilePaginationTest : BaseTest() {
    private lateinit var username: String

    override fun configureMockApi(mockApiServer: MockApiServer) {
        username = mockApiServer.profileUsername()
        mockApiServer.homepageLoggedOut()
        mockApiServer.profileWithShortFirstPage(username)
    }

    @Test
    fun profileKeepsLoadingPagesAfterAShortPage() {
        profile(activityRule) {
            openProfileLink(username)
            displayProfileList()

            scrollToLink(LinkFixtures.linkId(page = 1, index = ProfileRoutes.SHORT_FIRST_PAGE_LINKS))
            scrollToLink(LinkFixtures.linkId(page = 2, index = ProfileRoutes.LINKS_PER_PAGE))
            displayTitle(LinkFixtures.linkTitle(page = 2, index = ProfileRoutes.LINKS_PER_PAGE))
        }
    }
}
