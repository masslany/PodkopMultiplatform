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

    /**
     * Opens the details of the link with [id] by tapping its [title]. The middle of the card can be
     * its source, which opens the link's page instead.
     */
    fun openLink(
        id: Int,
        title: String,
    ) {
        scrollToLink(id)
        clickText(title)
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
