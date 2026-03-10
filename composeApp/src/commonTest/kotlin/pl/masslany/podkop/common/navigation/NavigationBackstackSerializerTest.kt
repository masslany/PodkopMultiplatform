package pl.masslany.podkop.common.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import pl.masslany.podkop.features.composer.ComposerBottomSheetScreen
import pl.masslany.podkop.features.composer.ComposerPrefill
import pl.masslany.podkop.features.composer.ComposerRequest
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen

class NavigationBackstackSerializerTest {

    @Test
    fun `serialize and deserialize preserve the backstack`() {
        val backStack = listOf(
            HomeScreen,
            EntryDetailsScreen.forEntryCommentReply(
                entryId = 44,
                entryCommentId = 7,
                author = "alice",
            ),
            ComposerBottomSheetScreen(
                resultKey = "composer",
                request = ComposerRequest.CreateEntry(
                    prefill = ComposerPrefill(
                        content = "hello",
                        adult = true,
                    ),
                ),
            ),
        )

        val serializedBackStack = NavigationBackstackSerializer.serialize(backStack)

        assertEquals(backStack, serializedBackStack?.let(NavigationBackstackSerializer::deserialize))
    }

    @Test
    fun `deserialize returns null for invalid payload`() {
        assertNull(NavigationBackstackSerializer.deserialize("not-json"))
    }
}
