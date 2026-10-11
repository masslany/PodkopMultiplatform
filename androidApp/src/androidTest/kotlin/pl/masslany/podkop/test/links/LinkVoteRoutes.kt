package pl.masslany.podkop.test.links

import pl.masslany.podkop.test.fixtures.LinkFixtures
import pl.masslany.podkop.test.links.LinkDetailsRoutes.LINKS_PATH
import pl.masslany.podkop.test.support.MockApiServer

object LinkVoteRoutes {
    /**
     * Upvoting link [linkId] as a signed-in user: the vote, and the link again, which the app
     * fetches to show the new count, here [upvotes]. Vote responses were not captured; the app
     * reads no body from them, so the vote answers 204 like other updates.
     */
    fun MockApiServer.upvoteLink(
        linkId: Int,
        title: String,
        upvotes: Int,
    ) {
        respond(method = "POST", path = "$LINKS_PATH/$linkId/votes/up")
        getJson(
            path = "$LINKS_PATH/$linkId",
            body = LinkFixtures.details(sample("link-details-user"), linkId, title, upvotes),
        )
    }
}
