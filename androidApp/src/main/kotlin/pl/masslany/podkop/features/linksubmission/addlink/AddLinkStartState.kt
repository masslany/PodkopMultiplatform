package pl.masslany.podkop.features.linksubmission.addlink

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.features.linksubmission.models.AddLinkSavedDraftState
import pl.masslany.podkop.features.linksubmission.models.AddLinkSimilarItemState

data class AddLinkStartState(
    val url: String,
    val draftedUrl: String?,
    val draftKey: String?,
    val currentDrafts: ImmutableList<AddLinkSavedDraftState>,
    val isCheckingDraft: Boolean,
    val isLoadingDrafts: Boolean,
    val deletingDraftKey: String?,
    val similarLinks: ImmutableList<AddLinkSimilarItemState>,
    val showUrlFormatError: Boolean,
) {
    val currentUrl: String
        get() = draftedUrl?.trim().takeUnless { it.isNullOrBlank() } ?: url.trim()

    val canContinue: Boolean
        get() = url.trim().isNotBlank() && !isCheckingDraft && deletingDraftKey == null

    val hasSavedDrafts: Boolean
        get() = currentDrafts.isNotEmpty()

    val hasSimilarLinks: Boolean
        get() = similarLinks.isNotEmpty()

    companion object {
        val initial = AddLinkStartState(
            url = "",
            draftedUrl = null,
            draftKey = null,
            currentDrafts = persistentListOf(),
            isCheckingDraft = false,
            isLoadingDrafts = false,
            deletingDraftKey = null,
            similarLinks = persistentListOf(),
            showUrlFormatError = false,
        )
    }
}
