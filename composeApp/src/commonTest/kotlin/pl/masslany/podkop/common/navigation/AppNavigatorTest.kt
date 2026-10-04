package pl.masslany.podkop.common.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.tag.TagScreen
import pl.masslany.podkop.testsupport.navigation.createTestAppNavigator

@OptIn(ExperimentalCoroutinesApi::class)
class AppNavigatorTest {
    @Test
    fun `openLink shows content links in the app`() = runTest {
        val sut = createTestAppNavigator(backgroundScope)
        runCurrent()

        sut.openLink("https://wykop.pl/wpis/123/some-slug#456")
        assertEquals(EntryDetailsScreen.forEntry(id = 123), sut.state.value.rootStack.last())

        sut.openLink("https://www.wykop.pl/link/456/some-slug/komentarz/789")
        assertEquals(LinkDetailsScreen(id = 456), sut.state.value.rootStack.last())

        sut.openLink("https://wykop.pl/ludzie/some_user")
        assertEquals(ProfileScreen(username = "some_user"), sut.state.value.rootStack.last())

        sut.openLink("https://wykop.pl/tag/heheszki/najlepsze")
        assertEquals(TagScreen(tag = "heheszki"), sut.state.value.rootStack.last())
    }

    @Test
    fun `openLink does not stack the screen that is already on top`() = runTest {
        val sut = createTestAppNavigator(backgroundScope)
        runCurrent()

        sut.openLink("https://wykop.pl/wpis/123")
        val stackSize = sut.state.value.rootStack.size
        sut.openLink("https://wykop.pl/wpis/123/some-slug")

        assertEquals(stackSize, sut.state.value.rootStack.size)
    }
}
