package pl.masslany.podkop.test.links

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pl.masslany.podkop.test.common.BaseTest
import pl.masslany.podkop.test.fixtures.LinkCommentFixtures
import pl.masslany.podkop.test.fixtures.LinkFixtures
import pl.masslany.podkop.test.home.HomepageRoutes.homepageSignedIn
import pl.masslany.podkop.test.home.robots.homepage
import pl.masslany.podkop.test.links.LinkDetailsRoutes.signedInLinkDetails
import pl.masslany.podkop.test.links.robots.linkDetails
import pl.masslany.podkop.test.support.MockApiServer

@RunWith(AndroidJUnit4::class)
class LinkDetailsCommentsTest : BaseTest() {
    override val signedIn = true

    private val linkId = LinkFixtures.linkId(page = 1, index = 1)
    private val title = LinkFixtures.linkTitle(page = 1, index = 1)

    override fun configureMockApi(mockApiServer: MockApiServer) {
        mockApiServer.homepageSignedIn()
        mockApiServer.signedInLinkDetails(linkId, title)
    }

    @Test
    fun linkCommentsLoadTheNextPageAfterScrolling() {
        homepage(activityRule) {
            openLink(linkId, title)
        }

        linkDetails(activityRule) {
            displayComment(LinkCommentFixtures.commentId(page = 1, index = 1))
            scrollToComment(LinkCommentFixtures.commentId(page = 1, index = 25))
            scrollToComment(LinkCommentFixtures.commentId(page = 2, index = 25))
            displayComment(LinkCommentFixtures.commentId(page = 2, index = 25))
        }
    }

    @Test
    fun showingMoreRepliesLoadsTheRestOfAThread() {
        val comment = LinkDetailsRoutes.FIRST_COMMENT_ID
        homepage(activityRule) {
            openLink(linkId, title)
        }

        linkDetails(activityRule) {
            // A comment arrives with its first two replies.
            displayReply(LinkCommentFixtures.replyId(comment, index = 2))
            showMoreReplies(comment)
            displayReply(LinkCommentFixtures.replyId(comment, index = 3))
        }
    }
}
