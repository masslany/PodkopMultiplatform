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

    fun scrollUntilTitleIsVisible(title: String) {
        scrollToIndex(
            tag = LinksTestTags.Screen.List,
            index = PAGE_ONE_LAST_ITEM_INDEX,
        )
        scrollToIndexWhenAvailable(
            tag = LinksTestTags.Screen.List,
            index = PAGE_TWO_TARGET_ITEM_INDEX,
        )
        displayTitle(title)
    }

    fun displayHomepageList() {
        displayedNode(LinksTestTags.Screen.List)
    }

    private companion object {
        const val STATIC_ITEMS_BEFORE_LINKS = 2
        const val PAGE_SIZE = 25
        const val PAGE_ONE_LAST_ITEM_INDEX = STATIC_ITEMS_BEFORE_LINKS + PAGE_SIZE - 1
        const val PAGE_TWO_TARGET_ITEM_INDEX = STATIC_ITEMS_BEFORE_LINKS + PAGE_SIZE + PAGE_SIZE - 1
    }
}

fun homepage(
    testRule: PodkopComposeRule,
    block: HomepageRobot.() -> Unit,
) = HomepageRobot(testRule).apply(block)
