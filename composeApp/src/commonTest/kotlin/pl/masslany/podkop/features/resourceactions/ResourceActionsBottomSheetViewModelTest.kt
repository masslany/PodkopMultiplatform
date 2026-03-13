package pl.masslany.podkop.features.resourceactions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import pl.masslany.podkop.features.composer.ComposerRequest

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
    fun `buildResourceLink builds link url`() {
        val result = buildResourceLink(
            ResourceActionsParams(
                resourceType = ResourceActionsType.Link,
                rootId = 42,
                rootSlug = "slug",
            ),
        )

        assertEquals("https://wykop.pl/link/42/slug", result)
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

    @Test
    fun `buildState includes edit entry action when entry is editable`() {
        val state = buildState(
            ResourceActionsParams(
                resourceType = ResourceActionsType.Entry,
                rootId = 123,
                canEdit = true,
            ),
        )

        assertTrue(state.actions.any { it.id == ResourceActionId.EditEntry })
    }

    @Test
    fun `buildState includes copy text action when copyable content is provided`() {
        val state = buildState(
            ResourceActionsParams(
                resourceType = ResourceActionsType.EntryComment,
                rootId = 123,
                childId = 456,
                copyContent = "original comment body",
            ),
        )

        val copyTextAction = state.actions.firstOrNull { it.id == ResourceActionId.CopyText }
        val localAction = assertIs<ResourceActionLocalAction.CopyToClipboard>(copyTextAction?.localAction)
        assertEquals("original comment body", localAction.value)
    }

    @Test
    fun `buildState omits copy text action when copyable content is missing`() {
        val state = buildState(
            ResourceActionsParams(
                resourceType = ResourceActionsType.Entry,
                rootId = 123,
                copyContent = null,
            ),
        )

        assertFalse(state.actions.any { it.id == ResourceActionId.CopyText })
    }

    @Test
    fun `buildState omits copy text action for links even if copyable content is provided`() {
        val state = buildState(
            ResourceActionsParams(
                resourceType = ResourceActionsType.Link,
                rootId = 42,
                rootSlug = "slug",
                copyContent = "should not be shown",
            ),
        )

        assertFalse(state.actions.any { it.id == ResourceActionId.CopyText })
    }

    @Test
    fun `buildState includes edit entry comment action when entry comment is editable`() {
        val state = buildState(
            ResourceActionsParams(
                resourceType = ResourceActionsType.EntryComment,
                rootId = 123,
                childId = 456,
                canEdit = true,
            ),
        )

        assertTrue(state.actions.any { it.id == ResourceActionId.EditEntryComment })
    }

    @Test
    fun `buildState includes edit link comment action when link comment is editable`() {
        val state = buildState(
            ResourceActionsParams(
                resourceType = ResourceActionsType.LinkComment,
                rootId = 42,
                rootSlug = "slug",
                childId = 777,
                canEdit = true,
            ),
        )

        assertTrue(state.actions.any { it.id == ResourceActionId.EditLinkComment })
    }

    @Test
    fun `buildState omits edit actions when resource is not editable`() {
        val entryState = buildState(
            ResourceActionsParams(
                resourceType = ResourceActionsType.Entry,
                rootId = 1,
                canEdit = false,
            ),
        )
        val entryCommentState = buildState(
            ResourceActionsParams(
                resourceType = ResourceActionsType.EntryComment,
                rootId = 1,
                childId = 2,
                canEdit = false,
            ),
        )
        val linkCommentState = buildState(
            ResourceActionsParams(
                resourceType = ResourceActionsType.LinkComment,
                rootId = 1,
                rootSlug = "slug",
                childId = 2,
                canEdit = false,
            ),
        )

        assertFalse(entryState.actions.any { it.id == ResourceActionId.EditEntry })
        assertFalse(entryCommentState.actions.any { it.id == ResourceActionId.EditEntryComment })
        assertFalse(linkCommentState.actions.any { it.id == ResourceActionId.EditLinkComment })
    }

    @Test
    fun `buildEditComposerRequest builds entry edit request with prefill`() {
        val request = buildEditComposerRequest(
            ResourceActionsParams(
                resourceType = ResourceActionsType.Entry,
                rootId = 123,
                content = "edited entry",
                adult = true,
                photoKey = "photo-key",
                photoUrl = "https://cdn.example/photo.jpg",
            ),
        )

        val edit = assertIs<ComposerRequest.EditEntry>(request)
        assertEquals(123, edit.entryId)
        assertEquals("edited entry", edit.prefill.content)
        assertEquals(true, edit.prefill.adult)
        assertEquals("photo-key", edit.prefill.photoKey)
        assertEquals("https://cdn.example/photo.jpg", edit.prefill.photoUrl)
    }

    @Test
    fun `buildEditComposerRequest builds entry comment edit request with prefill`() {
        val request = buildEditComposerRequest(
            ResourceActionsParams(
                resourceType = ResourceActionsType.EntryComment,
                rootId = 123,
                childId = 456,
                content = "edited comment",
                adult = false,
                photoKey = null,
                photoUrl = null,
            ),
        )

        val edit = assertIs<ComposerRequest.EditEntryComment>(request)
        assertEquals(123, edit.entryId)
        assertEquals(456, edit.commentId)
        assertEquals("edited comment", edit.prefill.content)
        assertEquals(false, edit.prefill.adult)
        assertEquals(null, edit.prefill.photoKey)
        assertEquals(null, edit.prefill.photoUrl)
    }

    @Test
    fun `buildEditComposerRequest builds link comment edit request with prefill`() {
        val request = buildEditComposerRequest(
            ResourceActionsParams(
                resourceType = ResourceActionsType.LinkComment,
                rootId = 42,
                childId = 777,
                content = "edited link comment",
                adult = true,
                photoKey = "k",
                photoUrl = "u",
            ),
        )

        val edit = assertIs<ComposerRequest.EditLinkComment>(request)
        assertEquals(42, edit.linkId)
        assertEquals(777, edit.commentId)
        assertEquals("edited link comment", edit.prefill.content)
        assertEquals(true, edit.prefill.adult)
        assertEquals("k", edit.prefill.photoKey)
        assertEquals("u", edit.prefill.photoUrl)
    }

    @Test
    fun `buildEditComposerRequest returns null when child id is missing for comment resources`() {
        val entryCommentRequest = buildEditComposerRequest(
            ResourceActionsParams(
                resourceType = ResourceActionsType.EntryComment,
                rootId = 1,
                childId = null,
            ),
        )
        val linkCommentRequest = buildEditComposerRequest(
            ResourceActionsParams(
                resourceType = ResourceActionsType.LinkComment,
                rootId = 1,
                childId = null,
            ),
        )

        assertNull(entryCommentRequest)
        assertNull(linkCommentRequest)
    }

    @Test
    fun `buildEditComposerRequest returns null for link resource`() {
        val request = buildEditComposerRequest(
            ResourceActionsParams(
                resourceType = ResourceActionsType.Link,
                rootId = 1,
                rootSlug = "slug",
            ),
        )

        assertNull(request)
    }
}
