package pl.masslany.podkop.features.linksubmission.linkdraft

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import pl.masslany.podkop.features.linksubmission.models.AddLinkSuggestedImageState
import pl.masslany.podkop.features.linksubmission.models.AddLinkTagSuggestionState

data class LinkDraftScreenState(
    val draftKey: String?,
    val currentUrl: String,
    val title: String,
    val description: String,
    val tags: ImmutableList<String>,
    val tagInput: String,
    val tagSuggestions: ImmutableList<AddLinkTagSuggestionState>,
    val suggestedImages: ImmutableList<AddLinkSuggestedImageState>,
    val selectedSuggestedImageIndex: Int?,
    val adult: Boolean,
    val photoKey: String?,
    val photoUrl: String?,
    val isLoadingDraft: Boolean,
    val isLoadingTagSuggestions: Boolean,
    val isMediaUploading: Boolean,
    val isPublishing: Boolean,
) {
    val currentTagAutocompleteQuery: String
        get() = normalizeLinkTagQuery(tagInput)

    val hasSuggestedImages: Boolean
        get() = suggestedImages.isNotEmpty()

    val canSubmit: Boolean
        get() = title.trim().isNotBlank() &&
            (tags.isNotEmpty() || normalizeLinkTags(tagInput).isNotEmpty()) &&
            draftKey != null &&
            !isLoadingDraft &&
            !isMediaUploading &&
            !isPublishing

    companion object {
        val initial = LinkDraftScreenState(
            draftKey = null,
            currentUrl = "",
            title = "",
            description = "",
            tags = persistentListOf(),
            tagInput = "",
            tagSuggestions = persistentListOf(),
            suggestedImages = persistentListOf(),
            selectedSuggestedImageIndex = null,
            adult = false,
            photoKey = null,
            photoUrl = null,
            isLoadingDraft = false,
            isLoadingTagSuggestions = false,
            isMediaUploading = false,
            isPublishing = false,
        )
    }
}

internal fun normalizeLinkTags(rawValue: String): List<String> = rawValue
    .split(TAG_SPLIT_REGEX)
    .map { tag ->
        normalizeLinkTagQuery(tag)
    }
    .filter(String::isNotBlank)
    .distinct()

internal fun normalizeLinkTagQuery(rawValue: String): String = rawValue
    .trim()
    .removePrefix("#")
    .lowercase()

internal fun mergeLinkTags(existing: List<String>, additions: List<String>): List<String> {
    if (additions.isEmpty()) {
        return existing
    }

    val seen = existing.toMutableSet()
    val merged = existing.toMutableList()
    additions.forEach { tag ->
        if (seen.add(tag)) {
            merged += tag
        }
    }
    return merged
}

internal fun formatAddLinkAbsoluteDate(value: LocalDateTime?): String {
    if (value == null) {
        return "-"
    }

    return buildString {
        append(value.day.toString().padStart(2, '0'))
        append('.')
        append(value.month.number.toString().padStart(2, '0'))
        append('.')
        append(value.year)
        append(", ")
        append(value.hour.toString().padStart(2, '0'))
        append(':')
        append(value.minute.toString().padStart(2, '0'))
    }
}

private val TAG_SPLIT_REGEX = Regex("[,\\s]+")
