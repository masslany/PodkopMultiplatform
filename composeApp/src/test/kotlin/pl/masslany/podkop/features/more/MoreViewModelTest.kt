package pl.masslany.podkop.features.more

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.features.more.models.MoreSectionItemType

class MoreViewModelTest {

    @Test
    fun `content section includes my wykop for logged in users`() {
        val result = buildMoreContentSectionItems(isLoggedIn = true)

        assertEquals(
            listOf(
                MoreSectionItemType.Hits,
                MoreSectionItemType.Rank,
                MoreSectionItemType.Search,
                MoreSectionItemType.MyWykop,
            ),
            result.map { it.type },
        )
    }

    @Test
    fun `content section hides my wykop for logged out users`() {
        val result = buildMoreContentSectionItems(isLoggedIn = false)

        assertEquals(
            listOf(
                MoreSectionItemType.Hits,
                MoreSectionItemType.Rank,
                MoreSectionItemType.Search,
            ),
            result.map { it.type },
        )
    }
}
