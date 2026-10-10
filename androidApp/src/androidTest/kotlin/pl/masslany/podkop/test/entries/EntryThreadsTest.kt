package pl.masslany.podkop.test.entries

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pl.masslany.podkop.test.common.BaseTest
import pl.masslany.podkop.test.entries.EntryThreadRoutes.guestEntryThread
import pl.masslany.podkop.test.entries.robots.entryDetails
import pl.masslany.podkop.test.fixtures.EntryThreadFixtures
import pl.masslany.podkop.test.home.HomepageRoutes.homepageLoggedOut
import pl.masslany.podkop.test.support.MockApiServer
import pl.masslany.podkop.test.support.enableThreadedEntryComments

@RunWith(AndroidJUnit4::class)
class EntryThreadsTest : BaseTest() {
    override fun configureMockApi(mockApiServer: MockApiServer) {
        enableThreadedEntryComments()
        mockApiServer.homepageLoggedOut()
        mockApiServer.guestEntryThread(ENTRY_ID)
        topLevelComments = EntryThreadFixtures.topLevelCount(mockApiServer.sample("entry-thread-guest"))
    }

    private var topLevelComments = 0

    @Test
    fun aThreadShowsItsInlinedReplyAndLoadsTheRest() {
        entryDetails(activityRule) {
            openEntry(ENTRY_ID)
            displayText(EntryThreadFixtures.commentText(1))
            displayText(EntryThreadFixtures.replyText(commentIndex = 1, index = 1))

            showMoreReplies(EntryThreadFixtures.commentId(1))
            displayText(EntryThreadFixtures.replyText(commentIndex = 1, index = 2))
        }
    }

    @Test
    fun threadLoadsMoreTopLevelCommentsAfterScrolling() {
        entryDetails(activityRule) {
            openEntry(ENTRY_ID)
            scrollToComment(EntryThreadFixtures.commentId(EntryThreadFixtures.PAGE_SIZE))
            scrollToComment(EntryThreadFixtures.commentId(topLevelComments))
            displayText(EntryThreadFixtures.commentText(topLevelComments))
        }
    }

    private companion object {
        const val ENTRY_ID = 9101
    }
}
