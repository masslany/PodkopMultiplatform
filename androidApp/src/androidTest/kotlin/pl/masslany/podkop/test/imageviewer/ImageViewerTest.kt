package pl.masslany.podkop.test.imageviewer

import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pl.masslany.podkop.test.common.BaseTest
import pl.masslany.podkop.test.entries.EntryRoutes
import pl.masslany.podkop.test.entries.EntryRoutes.signedInEntryDetails
import pl.masslany.podkop.test.entries.robots.entryDetails
import pl.masslany.podkop.test.fixtures.ImageFixtures
import pl.masslany.podkop.test.home.HomepageRoutes.homepageSignedIn
import pl.masslany.podkop.test.imageviewer.robots.imageViewer
import pl.masslany.podkop.test.support.MockApiServer

@RunWith(AndroidJUnit4::class)
class ImageViewerTest : BaseTest() {
    override val signedIn = true

    override fun configureMockApi(mockApiServer: MockApiServer) {
        mockApiServer.homepageSignedIn()
        mockApiServer.signedInEntryDetails(ENTRY_ID, photoUrl = mockApiServer.baseUrl + PHOTO_PATH.removePrefix("/"))
        mockApiServer.get(path = PHOTO_PATH, body = ImageFixtures.png(), contentType = "image/png")
    }

    @Test
    fun swipingThePhotoAwayClosesTheViewer() {
        openEntryPhoto()

        imageViewer(activityRule) {
            swipeImageAway()
            assertClosed()
        }

        entryDetails(activityRule) {
            displayText(EntryRoutes.entryText(ENTRY_ID))
        }
    }

    @Test
    fun aShortSlowDragKeepsTheViewerOpen() {
        openEntryPhoto()

        imageViewer(activityRule) {
            dragImageSlightly()
            displayImage()
        }
    }

    @Test
    fun backClosesOnlyTheViewer() {
        openEntryPhoto()

        pressBack()

        imageViewer(activityRule) {
            assertClosed()
        }

        entryDetails(activityRule) {
            displayText(EntryRoutes.entryText(ENTRY_ID))
        }
    }

    private fun openEntryPhoto() {
        entryDetails(activityRule) {
            openEntry(ENTRY_ID)
            openPhoto()
        }

        imageViewer(activityRule) {
            displayImage()
        }
    }

    private companion object {
        const val ENTRY_ID = 7201
        const val PHOTO_PATH = "/photos/entry.png"
    }
}
