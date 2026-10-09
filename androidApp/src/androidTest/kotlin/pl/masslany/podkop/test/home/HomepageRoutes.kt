package pl.masslany.podkop.test.home

import pl.masslany.podkop.test.fixtures.LinkFixtures
import pl.masslany.podkop.test.support.MockApiServer

object HomepageRoutes {
    const val LINKS_PATH = "/api/v3/links"
    const val HITS_PATH = "/api/v3/hits/links"

    private val hitsQuery = mapOf(
        "sort" to "day",
        "page" to "1",
    )

    /**
     * Guests get the homepage in numbered pages. The API reports far more links than a test scrolls
     * through, so the app prefetches the page after the one in view: three pages cover scrolling
     * into the second.
     */
    fun MockApiServer.homepageLoggedOut() {
        val sample = sample("links-homepage-guest")
        (1..3).forEach { page ->
            getJson(
                path = LINKS_PATH,
                query = mapOf(
                    "sort" to "newest",
                    "type" to "homepage",
                    "page" to "$page",
                ),
                body = LinkFixtures.numberedPage(sample, page),
            )
        }
        getJson(
            path = HITS_PATH,
            query = hitsQuery,
            body = sample("hits-links-empty").toString(),
        )
    }
}
