package pl.masslany.podkop.test.notifications

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.serialization.json.JsonObject
import org.junit.Test
import org.junit.runner.RunWith
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pl.masslany.podkop.test.common.BaseTest
import pl.masslany.podkop.test.entries.EntryRoutes
import pl.masslany.podkop.test.entries.EntryRoutes.signedInEntryDetails
import pl.masslany.podkop.test.fixtures.NotificationFixtures
import pl.masslany.podkop.test.home.HomepageRoutes.homepageSignedIn
import pl.masslany.podkop.test.notifications.NotificationsRoutes.groupedTagNotifications
import pl.masslany.podkop.test.notifications.NotificationsRoutes.markTagNotificationRead
import pl.masslany.podkop.test.notifications.robots.NotificationsRobot.Companion.groupedRowId
import pl.masslany.podkop.test.notifications.robots.notifications
import pl.masslany.podkop.test.support.MockApiServer

@RunWith(AndroidJUnit4::class)
class TagNotificationGroupsTest : BaseTest() {
    override val signedIn = true

    private lateinit var members: JsonObject
    private val firstGroupRow = groupedRowId(NotificationsRoutes.FIRST_GROUP_ID)

    override fun configureMockApi(mockApiServer: MockApiServer) {
        mockApiServer.homepageSignedIn()
        mockApiServer.groupedTagNotifications()
        members = mockApiServer.sample(NotificationsRoutes.MEMBERS_SAMPLE)
        mockApiServer.markTagNotificationRead(NotificationFixtures.memberId(members, page = 1, index = 1))
        mockApiServer.signedInEntryDetails(memberEntryId())
    }

    @Test
    fun expandedTagGroupLoadsMoreOfItsNotifications() {
        notifications(activityRule) {
            openNotifications()
            selectGroup(NotificationGroup.Tags)
            expandGroupedRow(firstGroupRow)

            scrollToGroupedRowNotification(firstGroupRow, NotificationFixtures.memberId(members, page = 1, index = 25))
            showMoreInGroupedRow(firstGroupRow)
            scrollToGroupedRowNotification(firstGroupRow, NotificationFixtures.memberId(members, page = 2, index = 25))
        }
    }

    @Test
    fun openingANotificationInAGroupMarksItRead() {
        val firstMember = NotificationFixtures.memberId(members, page = 1, index = 1)
        notifications(activityRule) {
            openNotifications()
            selectGroup(NotificationGroup.Tags)
            expandGroupedRow(firstGroupRow)
            openGroupedRowNotification(firstGroupRow, firstMember)
        }

        // Like on website, it opens what the notification is about and marks just that one read.
        awaitRequest(method = "GET", path = "${EntryRoutes.ENTRIES_PATH}/${memberEntryId()}")
        awaitRequest(method = "PUT", path = "${NotificationsRoutes.NOTIFICATIONS_PATH}/tags/$firstMember")
    }

    private fun memberEntryId(): Int =
        members.getValue("data").jsonArray.first().jsonObject.getValue("entry").jsonObject.getValue("id").jsonPrimitive.int
}
