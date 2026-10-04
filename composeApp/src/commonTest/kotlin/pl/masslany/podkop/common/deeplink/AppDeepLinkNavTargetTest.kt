package pl.masslany.podkop.common.deeplink

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import pl.masslany.podkop.features.privatemessages.PrivateMessagesScreen
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.tag.TagScreen

class AppDeepLinkNavTargetTest {
    @Test
    fun `login callbacks have no screen`() {
        assertNull(AppDeepLink.LoginCallback(token = "abc", refreshToken = "def").toNavTarget())
    }

    @Test
    fun `maps navigational links to their screens`() {
        assertEquals(ProfileScreen(username = "some_user"), AppDeepLink.Profile(username = "some_user").toNavTarget())
        assertEquals(TagScreen(tag = "heheszki"), AppDeepLink.Tag(name = "heheszki").toNavTarget())
        assertEquals(PrivateMessagesScreen, AppDeepLink.PrivateMessagesInbox.toNavTarget())
    }
}
