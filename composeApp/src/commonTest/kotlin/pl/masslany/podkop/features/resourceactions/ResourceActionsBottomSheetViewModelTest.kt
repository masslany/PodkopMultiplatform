package pl.masslany.podkop.features.resourceactions

import kotlin.test.Test
import kotlin.test.assertEquals

class ResourceActionsBottomSheetViewModelTest {

    @Test
    fun `buildResourceLink builds entry link`() {
        val result = buildResourceLink(
            ResourceActionsParams(
                resourceType = ResourceActionsType.Entry,
                rootId = 123,
            ),
        )

        assertEquals("https://wykop.pl/wpis/123", result)
    }

    @Test
    fun `buildResourceLink builds entry comment anchor link`() {
        val result = buildResourceLink(
            ResourceActionsParams(
                resourceType = ResourceActionsType.EntryComment,
                rootId = 123,
                childId = 456,
            ),
        )

        assertEquals("https://wykop.pl/wpis/123/#456", result)
    }

    @Test
    fun `buildResourceLink builds top level link comment url when parent is missing or same comment`() {
        val params = ResourceActionsParams(
            resourceType = ResourceActionsType.LinkComment,
            rootId = 42,
            rootSlug = "slug",
            childId = 777,
            parentId = 777,
        )

        val result = buildResourceLink(params)

        assertEquals("https://wykop.pl/link/42/slug/komentarz/777", result)
    }

    @Test
    fun `buildResourceLink builds reply link comment url with parent and anchor`() {
        val result = buildResourceLink(
            ResourceActionsParams(
                resourceType = ResourceActionsType.LinkComment,
                rootId = 42,
                rootSlug = "slug",
                childId = 777,
                parentId = 111,
            ),
        )

        assertEquals("https://wykop.pl/link/42/slug/komentarz/111#777", result)
    }
}
