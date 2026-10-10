package pl.masslany.podkop.test.links

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pl.masslany.podkop.test.common.BaseTest
import pl.masslany.podkop.test.fixtures.LinkFixtures
import pl.masslany.podkop.test.home.HomepageRoutes.homepageSignedIn
import pl.masslany.podkop.test.home.robots.homepage
import pl.masslany.podkop.test.links.LinkVoteRoutes.upvoteLink
import pl.masslany.podkop.test.support.MockApiServer

@RunWith(AndroidJUnit4::class)
class LinkVotingTest : BaseTest() {
    override val signedIn = true

    private val linkId = LinkFixtures.linkId(page = 1, index = 1)
    private var upvotes = 0

    override fun configureMockApi(mockApiServer: MockApiServer) {
        mockApiServer.homepageSignedIn()
        upvotes = LinkFixtures.upvotes(mockApiServer.sample("links-homepage-user-page-1"))
        mockApiServer.upvoteLink(linkId, LinkFixtures.linkTitle(page = 1, index = 1), upvotes = upvotes + 1)
    }

    @Test
    fun upvotingALinkShowsItsNewCount() {
        homepage(activityRule) {
            scrollToLink(linkId)
            displayUpvotes(linkId, upvotes)
            upvoteLink(linkId)
            displayUpvotes(linkId, upvotes + 1)
        }
    }
}
