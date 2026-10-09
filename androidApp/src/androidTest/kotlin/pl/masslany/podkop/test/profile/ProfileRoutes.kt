package pl.masslany.podkop.test.profile

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pl.masslany.podkop.test.fixtures.LinkFixtures
import pl.masslany.podkop.test.fixtures.with
import pl.masslany.podkop.test.fixtures.withoutImages
import pl.masslany.podkop.test.support.MockApiServer

object ProfileRoutes {
    const val PROFILE_PATH = "/api/v3/profile/users"
    const val LINKS_PER_PAGE = 25

    // As website served a real author's activity on 2026-10-09: 23 of 25 items on page one,
    // although `total` said hundreds more existed. Later pages were full again.
    const val SHORT_FIRST_PAGE_LINKS = 23

    private const val BADGE_ICON_PATH = "/cdn/badge-icon.svg"
    private const val BADGE_ICON = """<svg xmlns="http://www.w3.org/2000/svg" width="1" height="1"/>"""

    fun MockApiServer.profileUsername(): String =
        sample("profile-users").getValue("data").jsonObject.getValue("username").jsonPrimitive.content

    /** A profile whose activity tab, the one it opens on, starts with a short page. */
    fun MockApiServer.profileWithShortFirstPage(username: String) {
        getJson(
            path = "$PROFILE_PATH/$username",
            body = sample("profile-users").withoutImages().toString(),
        )
        getJson(
            path = "$PROFILE_PATH/$username/badges",
            body = badgesWithLocalIcons(),
        )
        get(
            path = BADGE_ICON_PATH,
            body = BADGE_ICON,
            contentType = "image/svg+xml",
        )
        val actions = sample("profile-actions")
        mapOf(1 to SHORT_FIRST_PAGE_LINKS, 2 to LINKS_PER_PAGE, 3 to LINKS_PER_PAGE).forEach { (page, links) ->
            getJson(
                path = "$PROFILE_PATH/$username/actions",
                query = mapOf("page" to "$page"),
                body = LinkFixtures.numberedLinkPage(actions, page, links),
            )
        }
    }

    /** Badges keep their real shape, with icons served by the mock server instead of the CDN. */
    private fun MockApiServer.badgesWithLocalIcons(): String {
        val badges = sample("profile-badges")
        val iconUrl = JsonPrimitive(baseUrl + BADGE_ICON_PATH.removePrefix("/"))
        val withLocalIcons = badges.getValue("data").jsonArray.map { element ->
            val badge = element.jsonObject
            val media = badge.getValue("media").jsonObject
            badge.with("media" to media.with("icon" to media.getValue("icon").jsonObject.with("url" to iconUrl)))
        }
        return badges.with("data" to JsonArray(withLocalIcons)).toString()
    }
}
