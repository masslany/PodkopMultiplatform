package pl.masslany.podkop.test.notifications.robots

import org.koin.core.context.GlobalContext
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.notifications.NotificationsScreen
import pl.masslany.podkop.features.notifications.NotificationsTestTags
import pl.masslany.podkop.features.notifications.components.groupedRowMemberKey
import pl.masslany.podkop.test.common.PodkopComposeRule
import pl.masslany.podkop.test.common.robots.BaseRobot

class NotificationsRobot(
    testRule: PodkopComposeRule,
) : BaseRobot(testRule) {
    /** Opens notifications the way the top bar's bell does. */
    fun openNotifications() {
        onUiThread {
            GlobalContext.get().get<AppNavigator>().navigateTo(NotificationsScreen)
        }
    }

    fun selectGroup(group: NotificationGroup) {
        displayedNode(NotificationsTestTags.Group.chip(group))
        clickNodeWithTag(NotificationsTestTags.Group.chip(group))
    }

    fun expandGroupedRow(rowId: String) {
        displayedNode(NotificationsTestTags.Grouped.expand(rowId))
        clickNodeWithTag(NotificationsTestTags.Grouped.expand(rowId))
    }

    /** Scrolls to a notification inside an expanded group, waiting for the page holding it to load. */
    fun scrollToGroupedRowNotification(
        rowId: String,
        id: String,
    ) {
        scrollToKey(
            tag = NotificationsTestTags.Screen.List,
            key = groupedRowMemberKey(rowId, id),
        )
    }

    fun openGroupedRowNotification(
        rowId: String,
        id: String,
    ) {
        scrollToGroupedRowNotification(rowId, id)
        clickNodeWithTag(NotificationsTestTags.Item.card(id))
    }

    fun showMoreInGroupedRow(rowId: String) {
        scrollToKey(
            tag = NotificationsTestTags.Screen.List,
            key = "$rowId/show-more",
        )
        clickNodeWithTag(NotificationsTestTags.Grouped.showMore(rowId))
    }

    companion object {
        /** The app keys a grouped row by its group id. */
        fun groupedRowId(groupId: String): String = "group:$groupId"
    }
}

fun notifications(
    testRule: PodkopComposeRule,
    block: NotificationsRobot.() -> Unit,
) = NotificationsRobot(testRule).apply(block)
