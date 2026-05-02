package pl.masslany.podkop.test.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import pl.masslany.podkop.test.common.BaseTest
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
            displayTitle(FIRST_PAGE_TITLE)

            scrollUntilTitleIsVisible(SECOND_PAGE_TITLE)
        }
    }

    private companion object {
        const val FIRST_PAGE_TITLE = "Izraelskie siły zburzyły klasztor i szkołę sióstr w Yaroun (Liban)"
        const val SECOND_PAGE_TITLE = "Historia śmierci księdza Popiełuszki dalej nie jest wyjaśniona."
    }
}
