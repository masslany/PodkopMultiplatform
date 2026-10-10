package pl.masslany.podkop.test.links

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import pl.masslany.podkop.test.fixtures.LinkCommentFixtures
import pl.masslany.podkop.test.fixtures.with
import pl.masslany.podkop.test.fixtures.withoutImages
import pl.masslany.podkop.test.support.MockApiServer

object LinkDetailsRoutes {
    const val LINKS_PATH = "/api/v3/links"

    /** The comment whose replies [signedInLinkDetails] serves. */
    val FIRST_COMMENT_ID = LinkCommentFixtures.commentId(page = 1, index = 1)

    /**
     * A link as a signed-in user opens it: who is viewing (`profile/short`), the link titled [title],
     * its related links, and its comments sorted by `best`. The app prefetches the page after the one
     * in view, so three pages cover scrolling into the second. The first comment's replies are
     * served too.
     */
    fun MockApiServer.signedInLinkDetails(
        linkId: Int,
        title: String,
    ) {
        getJson(
            path = "/api/v3/profile/short",
            body = sample("profile-short-user").withoutImages().toString(),
        )
        val details = sample("link-details-user")
        getJson(
            path = "$LINKS_PATH/$linkId",
            body = details.with(
                "data" to details.getValue("data").jsonObject.withoutImages().with(
                    "id" to JsonPrimitive(linkId),
                    "title" to JsonPrimitive(title),
                ),
            ).toString(),
        )
        getJson(
            path = "$LINKS_PATH/$linkId/related",
            body = sample("link-related-user").withoutImages().toString(),
        )
        val comments = sample("link-comments-user")
        (1..3).forEach { page ->
            getJson(
                path = "$LINKS_PATH/$linkId/comments",
                query = mapOf("page" to "$page", "sort" to "best"),
                body = LinkCommentFixtures.commentsPage(comments, linkId, page),
            )
        }
        val replies = sample("link-comment-replies-user")
        (1..2).forEach { page ->
            getJson(
                path = "$LINKS_PATH/$linkId/comments/$FIRST_COMMENT_ID/comments",
                query = mapOf("page" to "$page"),
                body = LinkCommentFixtures.repliesPage(replies, linkId, FIRST_COMMENT_ID, page),
            )
        }
    }
}
