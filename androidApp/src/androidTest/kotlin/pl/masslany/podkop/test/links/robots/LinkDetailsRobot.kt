package pl.masslany.podkop.test.links.robots

import pl.masslany.podkop.features.linkdetails.LinkDetailsTestTags
import pl.masslany.podkop.test.common.PodkopComposeRule
import pl.masslany.podkop.test.common.robots.BaseRobot
import pl.masslany.podkop.test.fixtures.LinkCommentFixtures

class LinkDetailsRobot(
    testRule: PodkopComposeRule,
) : BaseRobot(testRule) {
    /** Scrolls to the comment with [id], waiting for the page holding it to load. */
    fun scrollToComment(id: Int) {
        scrollToKey(
            tag = LinkDetailsTestTags.Screen.List,
            key = id,
        )
    }

    fun displayComment(id: Int) {
        displayedText(LinkCommentFixtures.commentText(id))
    }

    fun displayReply(id: Int) {
        displayedText(LinkCommentFixtures.replyText(id))
    }

    fun showMoreReplies(commentId: Int) {
        displayedNode(LinkDetailsTestTags.Comment.showMoreReplies(commentId))
        clickNodeWithTag(LinkDetailsTestTags.Comment.showMoreReplies(commentId))
    }
}

fun linkDetails(
    testRule: PodkopComposeRule,
    block: LinkDetailsRobot.() -> Unit,
) = LinkDetailsRobot(testRule).apply(block)
