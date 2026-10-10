package pl.masslany.podkop.test.links

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pl.masslany.podkop.test.common.BaseTest
import pl.masslany.podkop.test.entries.EntryRoutes
import pl.masslany.podkop.test.entries.EntryRoutes.signedInEntryDetails
import pl.masslany.podkop.test.fixtures.LinkFixtures
import pl.masslany.podkop.test.home.HomepageRoutes.homepageSignedIn
import pl.masslany.podkop.test.home.robots.homepage
import pl.masslany.podkop.test.support.MockApiServer
import pl.masslany.podkop.test.tag.TagRoutes
import pl.masslany.podkop.test.tag.TagRoutes.signedInTagStream
import pl.masslany.podkop.test.tag.robots.tag

/** Links to the website's own content open in the app rather than in a browser. */
@RunWith(AndroidJUnit4::class)
class WebsiteLinksTest : BaseTest() {
    override val signedIn = true

    private val linkToEntry = LinkFixtures.linkId(page = 1, index = 1)
    private val linkToTag = LinkFixtures.linkId(page = 1, index = 2)

    private val entryUrl = "https://wykop.pl/wpis/$ENTRY_ID/sample-entry"
    private val tagUrl = "https://wykop.pl/tag/$TAG"

    override fun configureMockApi(mockApiServer: MockApiServer) {
        mockApiServer.homepageSignedIn(sources = mapOf(linkToEntry to entryUrl, linkToTag to tagUrl))
        mockApiServer.signedInEntryDetails(ENTRY_ID)
        mockApiServer.signedInTagStream(tagName = TAG, type = "all")
    }

    @Test
    fun aLinkToAnEntryOnTheWebsiteOpensTheEntry() {
        homepage(activityRule) {
            openSource(linkToEntry, LinkFixtures.sourceLabel(entryUrl))
            displayText(EntryRoutes.entryText(ENTRY_ID))
        }
    }

    @Test
    fun aLinkToATagOnTheWebsiteOpensTheTag() {
        homepage(activityRule) {
            openSource(linkToTag, LinkFixtures.sourceLabel(tagUrl))
        }

        tag(activityRule) {
            displayStreamItem(TagRoutes.streamItemText(type = "all", index = 1))
        }
    }

    private companion object {
        const val ENTRY_ID = 7101
        const val TAG = "sampletag"
    }
}
