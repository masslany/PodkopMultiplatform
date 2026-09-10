package pl.masslany.podkop.test.home

import pl.masslany.podkop.test.support.MockApiServer

object HomepageRoutes {
    const val LINKS_PATH = "/api/v3/links"
    const val HITS_PATH = "/api/v3/hits/links"

    val linksPageOneQuery = mapOf(
        "sort" to "newest",
        "type" to "homepage",
        "page" to "1",
    )

    val linksPageTwoQuery = mapOf(
        "sort" to "newest",
        "type" to "homepage",
        "page" to "2",
    )

    private val hitsQuery = mapOf(
        "sort" to "day",
        "page" to "1",
    )

    fun MockApiServer.homepageLoggedOut() {
        getJson(
            path = LINKS_PATH,
            query = linksPageOneQuery,
            assetPath = "mock-api/homepage-links-page-1.json",
        )
        getJson(
            path = LINKS_PATH,
            query = linksPageTwoQuery,
            assetPath = "mock-api/homepage-links-page-2.json",
        )
        getJson(
            path = HITS_PATH,
            query = hitsQuery,
            assetPath = "mock-api/homepage-hits.json",
        )
    }
}
