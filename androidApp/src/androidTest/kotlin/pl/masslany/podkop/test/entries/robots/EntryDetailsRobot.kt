package pl.masslany.podkop.test.entries.robots

import org.koin.core.context.GlobalContext
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.entrydetails.EntryDetailsTestTags
import pl.masslany.podkop.test.common.PodkopComposeRule
import pl.masslany.podkop.test.common.robots.BaseRobot

class EntryDetailsRobot(
    testRule: PodkopComposeRule,
) : BaseRobot(testRule) {
    /** Opens the entry the way tapping it in a feed does. */
    fun openEntry(id: Int) {
        onUiThread {
            GlobalContext.get().get<AppNavigator>().navigateTo(EntryDetailsScreen(id))
        }
    }

    fun displayText(text: String) {
        displayedText(text)
    }

    /** Scrolls to the thread's comment with [id], waiting for the page holding it to load. */
    fun scrollToComment(id: Int) {
        scrollToKey(
            tag = EntryDetailsTestTags.Screen.List,
            key = "comment-$id",
        )
    }

    fun showMoreReplies(parentId: Int) {
        scrollToKey(
            tag = EntryDetailsTestTags.Screen.List,
            key = "more-$parentId",
        )
        clickNodeWithTag(EntryDetailsTestTags.Thread.moreReplies(parentId))
    }
}

fun entryDetails(
    testRule: PodkopComposeRule,
    block: EntryDetailsRobot.() -> Unit,
) = EntryDetailsRobot(testRule).apply(block)
