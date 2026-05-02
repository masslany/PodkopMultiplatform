package pl.masslany.podkop.test.common.robots

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex

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

    protected fun clickNodeWithTag(tag: String) {
        nodeWithTag(tag).performClick()
    }

    protected fun scrollToIndex(
        tag: String,
        index: Int,
    ) {
        nodeWithTag(tag).performScrollToIndex(index)
    }

    protected fun scrollToIndexWhenAvailable(
        tag: String,
        index: Int,
        timeoutMillis: Long = DEFAULT_TIMEOUT_MS,
    ) {
        waitUntil(timeoutMillis) {
            runCatching {
                scrollToIndex(tag, index)
            }.isSuccess
        }
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
