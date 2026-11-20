package pl.masslany.podkop.features.linksubmission.linkdraft

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.links.domain.models.LinkDraftDetails
import pl.masslany.podkop.business.links.domain.models.request.PublishLinkDraft
import pl.masslany.podkop.business.links.domain.models.request.UpdateLinkDraft
import pl.masslany.podkop.features.linksubmission.models.AddLinkSavedDraftState
import pl.masslany.podkop.features.linksubmission.models.AddLinkSimilarItemState
import pl.masslany.podkop.features.linksubmission.models.AddLinkSuggestedImageState
import pl.masslany.podkop.features.linksubmission.models.AddLinkTagSuggestionState

internal fun LinkDraftScreenState.withLoadedDraft(
    draftDetails: LinkDraftDetails,
): LinkDraftScreenState = copy(
    draftKey = draftDetails.key,
    currentUrl = draftDetails.url,
    title = draftDetails.title.orEmpty(),
    description = draftDetails.description.orEmpty(),
    tags = draftDetails.tags.toImmutableList(),
    tagInput = "",
    tagSuggestions = emptyList<AddLinkTagSuggestionState>().toImmutableList(),
    suggestedImages = draftDetails.toSuggestedImageStates(),
    selectedSuggestedImageIndex = draftDetails.selectedImageIndex,
    adult = draftDetails.adult,
    photoKey = draftDetails.photoKey,
    photoUrl = draftDetails.photoUrl,
    isLoadingDraft = false,
    isLoadingTagSuggestions = false,
    isMediaUploading = false,
    isPublishing = false,
)

internal fun LinkDraftScreenState.withTagInputChanged(value: String): LinkDraftScreenState {
    val trailingToken = value.takeLastWhile { !it.isTagSeparator() }
    val completedChunk = if (trailingToken == value) "" else value.dropLast(trailingToken.length)
    val mergedTags = mergeLinkTags(tags, normalizeLinkTags(completedChunk))
    return copy(
        tagInput = trailingToken,
        tags = mergedTags.toImmutableList(),
    )
}

internal fun LinkDraftScreenState.withPendingTagSubmitted(): LinkDraftScreenState {
    val additions = normalizeLinkTags(tagInput)
    if (additions.isEmpty()) {
        return this
    }

    return copy(
        tags = mergeLinkTags(tags, additions).toImmutableList(),
        tagInput = "",
        tagSuggestions = emptyList<AddLinkTagSuggestionState>().toImmutableList(),
        isLoadingTagSuggestions = false,
    )
}

internal fun LinkDraftScreenState.withTagSuggestionSelected(tag: String): LinkDraftScreenState {
    val normalizedTag = normalizeLinkTagQuery(tag)
    if (normalizedTag.isBlank()) {
        return this
    }

    return copy(
        tags = mergeLinkTags(tags, listOf(normalizedTag)).toImmutableList(),
        tagInput = "",
        tagSuggestions = emptyList<AddLinkTagSuggestionState>().toImmutableList(),
        isLoadingTagSuggestions = false,
    )
}

internal fun LinkDraftScreenState.toValidatedPublishRequest(): PublishLinkDraft? {
    val normalizedTitle = title.trim()
    val normalizedTags = mergeLinkTags(
        existing = tags,
        additions = normalizeLinkTags(tagInput),
    )

    if (normalizedTitle.isBlank() || normalizedTags.isEmpty()) {
        return null
    }

    return PublishLinkDraft(
        title = normalizedTitle,
        description = description.trim().ifBlank { null },
        tags = normalizedTags,
        photoKey = photoKey,
        adult = adult,
        selectedImageIndex = selectedSuggestedImageIndex,
    )
}

internal fun LinkDraftScreenState.toUpdateLinkDraftRequest(selectedImageIndex: Int?): UpdateLinkDraft = UpdateLinkDraft(
    title = title.trim(),
    description = description.trim().ifBlank { null },
    tags = tags,
    photoKey = photoKey,
    adult = adult,
    selectedImageIndex = selectedImageIndex,
)

internal fun LinkDraftScreenState.toDismissSaveRequest(): UpdateLinkDraft = UpdateLinkDraft(
    title = title.trim(),
    description = description.trim().ifBlank { null },
    tags = mergeLinkTags(tags, normalizeLinkTags(tagInput)),
    photoKey = photoKey,
    adult = adult,
    selectedImageIndex = selectedSuggestedImageIndex,
)

internal fun LinkDraftDetails.toSuggestedImageStates(): ImmutableList<AddLinkSuggestedImageState> = suggestedImages
    .map(::AddLinkSuggestedImageState)
    .toImmutableList()

internal fun ResourceItem.toAddLinkSimilarItemState(): AddLinkSimilarItemState = AddLinkSimilarItemState(
    id = id,
    title = title,
    digCount = votes?.up ?: 0,
    createdAt = formatAddLinkAbsoluteDate(createdAt),
    sourceLabel = source?.label ?: source?.url.orEmpty(),
)

internal fun LinkDraftDetails.toSavedDraftState(): AddLinkSavedDraftState = AddLinkSavedDraftState(
    key = key,
    title = title.orEmpty(),
    url = url,
)

private fun Char.isTagSeparator(): Boolean = this == ',' || this.isWhitespace()
