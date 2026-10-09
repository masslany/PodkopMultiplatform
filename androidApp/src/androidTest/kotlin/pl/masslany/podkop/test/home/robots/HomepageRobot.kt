package pl.masslany.podkop.test.home.robots

import pl.masslany.podkop.features.links.LinksTestTags
import pl.masslany.podkop.test.common.PodkopComposeRule
import pl.masslany.podkop.test.common.robots.BaseRobot

class HomepageRobot(
    testRule: PodkopComposeRule,
) : BaseRobot(testRule) {
    fun displayTitle(title: String) {
        displayedText(title)
    }

    fun displayHomepageList() {
        displayedNode(LinksTestTags.Screen.List)
    }

    /** Scrolls the homepage to the link with [id], waiting for the page holding it to load. */
    fun scrollToLink(id: Int) {
        scrollToKey(
            tag = LinksTestTags.Screen.List,
            key = id,
        )
    }
}

fun homepage(
    testRule: PodkopComposeRule,
    block: HomepageRobot.() -> Unit,
) = HomepageRobot(testRule).apply(block)
