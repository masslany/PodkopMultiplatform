package pl.masslany.podkop.features.linksubmission.linkdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.business.links.domain.models.LinkDraftDetails
import pl.masslany.podkop.features.linksubmission.models.AddLinkTagSuggestionState

class AddLinkStateExtensionsTest {

    @Test
    fun `withLoadedDraft replaces edit state with loaded draft payload`() {
        val result = LinkDraftScreenState.initial.copy(
            draftKey = "old",
            currentUrl = "https://wrong.example",
            title = "Edited",
            description = "Edited",
            tags = persistentListOf("local"),
            tagInput = "pend",
            tagSuggestions = persistentListOf(AddLinkTagSuggestionState("pending", 10)),
            isLoadingDraft = true,
            isLoadingTagSuggestions = true,
            isMediaUploading = true,
            isPublishing = true,
        ).withLoadedDraft(
            draftDetails(
                key = "saved",
                url = "https://saved.example/article",
                title = "Saved title",
                description = "Saved description",
                tags = listOf("ai", "tech"),
                adult = true,
                photoKey = "photo-key",
                photoUrl = "https://cdn.example/photo.jpg",
                suggestedImages = listOf("https://cdn.example/1.jpg", "https://cdn.example/2.jpg"),
                selectedImageIndex = 1,
            ),
        )

        assertEquals("saved", result.draftKey)
        assertEquals("https://saved.example/article", result.currentUrl)
        assertEquals("Saved title", result.title)
        assertEquals("Saved description", result.description)
        assertEquals(listOf("ai", "tech"), result.tags)
        assertEquals("", result.tagInput)
        assertEquals(emptyList(), result.tagSuggestions)
        assertEquals(true, result.adult)
        assertEquals("photo-key", result.photoKey)
        assertEquals("https://cdn.example/photo.jpg", result.photoUrl)
        assertEquals(
            listOf("https://cdn.example/1.jpg", "https://cdn.example/2.jpg"),
            result.suggestedImages.map {
                it.url
            },
        )
        assertEquals(1, result.selectedSuggestedImageIndex)
        assertEquals(false, result.isLoadingDraft)
        assertEquals(false, result.isLoadingTagSuggestions)
        assertEquals(false, result.isMediaUploading)
        assertEquals(false, result.isPublishing)
    }

    @Test
    fun `withTagInputChanged promotes completed tags and keeps trailing token for autocomplete`() {
        val result = LinkDraftScreenState.initial.copy(
            tags = persistentListOf("polityka"),
        ).withTagInputChanged(" #AI, #Tech szcz")

        assertEquals(listOf("polityka", "ai", "tech"), result.tags)
        assertEquals("szcz", result.tagInput)
    }

    @Test
    fun `withPendingTagSubmitted appends normalized pending tags and clears suggestions`() {
        val result = LinkDraftScreenState.initial.copy(
            tags = persistentListOf("ai"),
            tagInput = " #Tech #AI nauka ",
            tagSuggestions = persistentListOf(AddLinkTagSuggestionState("tech", 10)),
            isLoadingTagSuggestions = true,
        ).withPendingTagSubmitted()

        assertEquals(listOf("ai", "tech", "nauka"), result.tags)
        assertEquals("", result.tagInput)
        assertEquals(emptyList(), result.tagSuggestions)
        assertEquals(false, result.isLoadingTagSuggestions)
    }

    @Test
    fun `withTagSuggestionSelected adds normalized tag and clears input`() {
        val result = LinkDraftScreenState.initial.copy(
            tags = persistentListOf("ai"),
            tagInput = "szcz",
            tagSuggestions = persistentListOf(AddLinkTagSuggestionState("szczecin", 100)),
            isLoadingTagSuggestions = true,
        ).withTagSuggestionSelected("#Szczecin")

        assertEquals(listOf("ai", "szczecin"), result.tags)
        assertEquals("", result.tagInput)
        assertEquals(emptyList(), result.tagSuggestions)
        assertEquals(false, result.isLoadingTagSuggestions)
    }

    @Test
    fun `toValidatedPublishRequest trims fields merges pending tags and includes selected image`() {
        val result = LinkDraftScreenState.initial.copy(
            draftKey = "draft-key",
            title = "  Asus Co  ",
            description = "  Desc  ",
            tags = persistentListOf("ai"),
            tagInput = " #Tech ",
            photoKey = "photo-key",
            adult = true,
            selectedSuggestedImageIndex = 2,
        ).toValidatedPublishRequest()

        requireNotNull(result)
        assertEquals("Asus Co", result.title)
        assertEquals("Desc", result.description)
        assertEquals(listOf("ai", "tech"), result.tags)
        assertEquals("photo-key", result.photoKey)
        assertEquals(true, result.adult)
        assertEquals(2, result.selectedImageIndex)
    }

    @Test
    fun `toValidatedPublishRequest returns null when title or tags are missing after normalization`() {
        val missingTitle = LinkDraftScreenState.initial.copy(
            title = " ",
            tags = persistentListOf("ai"),
        ).toValidatedPublishRequest()
        val missingTags = LinkDraftScreenState.initial.copy(
            title = "Title",
            tags = persistentListOf(),
            tagInput = "  ",
        ).toValidatedPublishRequest()

        assertNull(missingTitle)
        assertNull(missingTags)
    }

    @Test
    fun `toUpdateLinkDraftRequest trims description and preserves selected image`() {
        val result = LinkDraftScreenState.initial.copy(
            title = " Title ",
            description = "  Description  ",
            tags = persistentListOf("ai"),
            photoKey = "photo-key",
            adult = false,
        ).toUpdateLinkDraftRequest(selectedImageIndex = 3)

        assertEquals("Title", result.title)
        assertEquals("Description", result.description)
        assertEquals(listOf("ai"), result.tags)
        assertEquals("photo-key", result.photoKey)
        assertEquals(false, result.adult)
        assertEquals(3, result.selectedImageIndex)
    }

    @Test
    fun `toDismissSaveRequest merges pending tags and keeps selected image`() {
        val result = LinkDraftScreenState.initial.copy(
            title = " Title ",
            description = "  Description  ",
            tags = persistentListOf("ai"),
            tagInput = " #Tech ",
            photoKey = "photo-key",
            adult = true,
            selectedSuggestedImageIndex = 1,
        ).toDismissSaveRequest()

        assertEquals("Title", result.title)
        assertEquals("Description", result.description)
        assertEquals(listOf("ai", "tech"), result.tags)
        assertEquals("photo-key", result.photoKey)
        assertEquals(true, result.adult)
        assertEquals(1, result.selectedImageIndex)
    }

    private fun draftDetails(
        key: String = "draft-key",
        url: String = "https://example.com/article",
        title: String? = null,
        description: String? = null,
        tags: List<String> = emptyList(),
        adult: Boolean = false,
        photoKey: String? = null,
        photoUrl: String? = null,
        suggestedImages: List<String> = emptyList(),
        selectedImageIndex: Int? = null,
    ) = LinkDraftDetails(
        key = key,
        url = url,
        title = title,
        description = description,
        tags = tags,
        adult = adult,
        photoKey = photoKey,
        photoUrl = photoUrl,
        suggestedImages = suggestedImages,
        selectedImageIndex = selectedImageIndex,
    )
}
