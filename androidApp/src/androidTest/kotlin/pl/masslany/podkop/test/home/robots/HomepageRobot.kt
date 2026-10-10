package pl.masslany.podkop.test.home.robots

import pl.masslany.podkop.common.components.CommonTestTags
import pl.masslany.podkop.features.links.LinksTestTags
import pl.masslany.podkop.features.resources.ResourcesTestTags
import pl.masslany.podkop.test.common.PodkopComposeRule
import pl.masslany.podkop.test.common.robots.BaseRobot

class HomepageRobot(
    testRule: PodkopComposeRule,
) : BaseRobot(testRule) {
    fun displayTitle(title: String) {
        displayedText(title)
    }

    /** The screen shown when the homepage could not load, with a button to try again. */
    fun displayLoadError() {
        displayedNode(CommonTestTags.Error.Screen)
    }

    fun retry() {
        clickNodeWithTag(CommonTestTags.Error.Retry)
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

    fun upvoteLink(id: Int) {
        clickNodeWithTag(ResourcesTestTags.Link.vote(id))
    }

    fun displayUpvotes(
        id: Int,
        upvotes: Int,
    ) {
        displayedTextInNode(ResourcesTestTags.Link.vote(id), upvotes.toString())
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
