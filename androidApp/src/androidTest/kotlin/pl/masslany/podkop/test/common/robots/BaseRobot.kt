package pl.masslany.podkop.test.common.robots

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey

open class BaseRobot(
    private val testRule: AndroidComposeTestRule<*, *>,
) {
    protected fun displayedText(
        text: String,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MS,
    ) {
        waitUntilText(text, timeoutMillis)
        testRule
            .onAllNodesWithText(text, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    protected fun displayedNode(
        tag: String,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MS,
    ) {
        waitUntilNode(tag, timeoutMillis)
        nodeWithTag(tag).assertIsDisplayed()
    }

    /** Waits until the node tagged [tag] shows [text], e.g. a count that changes. */
    protected fun displayedTextInNode(
        tag: String,
        text: String,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MS,
    ) {
        val matcher = hasTestTag(tag) and hasAnyDescendant(hasText(text))
        testRule.waitUntil(timeoutMillis) {
            testRule.onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        testRule.onNode(matcher, useUnmergedTree = true).assertIsDisplayed()
    }

    protected fun onUiThread(action: () -> Unit) {
        testRule.runOnUiThread(action)
    }

    protected fun clickNodeWithTag(tag: String) {
        nodeWithTag(tag).performClick()
    }

    /** Taps [text], e.g. a title, which reaches whatever handles taps around it, like a card. */
    protected fun clickText(text: String) {
        waitUntilText(text)
        testRule
            .onAllNodesWithText(text, useUnmergedTree = true)
            .onFirst()
            .performClick()
    }

    /**
     * Waits until the lazy list tagged [tag] is shown and holds an item with [key], e.g. once its
     * page loads, then scrolls to it.
     */
    protected fun scrollToKey(
        tag: String,
        key: Any,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MS,
    ) {
        waitUntil(timeoutMillis) {
            val list = testRule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().firstOrNull()
            val indexForKey = list?.config?.getOrNull(SemanticsProperties.IndexForKey)
            indexForKey != null && indexForKey(key) >= 0
        }
        nodeWithTag(tag).performScrollToKey(key)
    }

    protected fun waitUntilText(
        text: String,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MS,
    ) {
        testRule.waitUntil(timeoutMillis) {
            testRule
                .onAllNodesWithText(text, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    protected fun waitUntilNode(
        tag: String,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MS,
    ) {
        testRule.waitUntil(timeoutMillis) {
            testRule
                .onAllNodesWithTag(tag, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    protected fun waitUntil(
        timeoutMillis: Long = DEFAULT_TIMEOUT_MS,
        condition: () -> Boolean,
    ) {
        testRule.waitUntil(timeoutMillis = timeoutMillis, condition = condition)
    }

    private fun nodeWithTag(tag: String): SemanticsNodeInteraction =
        testRule.onNodeWithTag(tag, useUnmergedTree = true)

    protected companion object {
        const val DEFAULT_TIMEOUT_MS = 10_000L
    }
}
