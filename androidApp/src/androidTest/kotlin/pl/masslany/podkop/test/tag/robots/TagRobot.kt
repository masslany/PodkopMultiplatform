package pl.masslany.podkop.test.tag.robots

import pl.masslany.podkop.test.common.PodkopComposeRule
import pl.masslany.podkop.test.common.robots.BaseRobot

class TagRobot(
    testRule: PodkopComposeRule,
) : BaseRobot(testRule) {
    /** Checks the stream shows an entry's content or a link's title. */
    fun displayStreamItem(text: String) {
        displayedText(text)
    }
}

fun tag(
    testRule: PodkopComposeRule,
    block: TagRobot.() -> Unit,
) = TagRobot(testRule).apply(block)
