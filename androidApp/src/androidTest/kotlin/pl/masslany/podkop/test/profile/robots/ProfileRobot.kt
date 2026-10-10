package pl.masslany.podkop.test.profile.robots

import org.koin.core.context.GlobalContext
import pl.masslany.podkop.common.deeplink.AppDeepLinkHandler
import pl.masslany.podkop.features.profile.ProfileTestTags
import pl.masslany.podkop.test.common.PodkopComposeRule
import pl.masslany.podkop.test.common.robots.BaseRobot

class ProfileRobot(
    testRule: PodkopComposeRule,
) : BaseRobot(testRule) {
    /** Opens a website profile link the way the app handles any incoming link. */
    fun openProfileLink(username: String) {
        onUiThread {
            GlobalContext.get().get<AppDeepLinkHandler>().onIncomingUrl("https://wykop.pl/ludzie/$username")
        }
    }

    fun displayProfileList() {
        displayedNode(ProfileTestTags.Screen.List)
    }

    fun displayTitle(title: String) {
        displayedText(title)
    }

    /** Scrolls the profile list to the link with [id], waiting for the page holding it to load. */
    fun scrollToLink(id: Int) {
        // The profile list keys resources by their content type and id.
        scrollToKey(
            tag = ProfileTestTags.Screen.List,
            key = "LinkItem_$id",
        )
    }
}

fun profile(
    testRule: PodkopComposeRule,
    block: ProfileRobot.() -> Unit,
) = ProfileRobot(testRule).apply(block)
