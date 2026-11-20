package pl.masslany.podkop.features.linksubmission.linkdraft

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
internal interface LinkDraftActions : TopBarActions {
    fun onTitleChanged(value: String)
    fun onDescriptionChanged(value: String)
    fun onTagInputChanged(value: String)
    fun onPendingTagSubmitted()
    fun onTagSuggestionClicked(tag: String)
    fun onTagRemoved(tag: String)
    fun onAdultChanged(value: Boolean)
    fun onSuggestedImageChanged(index: Int)
    fun onPhotoAttachClicked()
    fun onPhotoRemoved()
    fun onSubmitClicked()
    fun onCancelClicked()
}
