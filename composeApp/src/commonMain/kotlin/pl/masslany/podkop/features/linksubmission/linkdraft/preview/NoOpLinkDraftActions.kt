package pl.masslany.podkop.features.linksubmission.linkdraft.preview

import pl.masslany.podkop.features.linksubmission.linkdraft.LinkDraftActions

object NoOpLinkDraftActions : LinkDraftActions {
    override fun onTopBarBackClicked() = Unit
    override fun onTopBarSearchClicked() = Unit
    override fun onTopBarNotificationsClicked() = Unit
    override fun onTopBarAddEntryClicked() = Unit
    override fun onTopBarAddLinkClicked() = Unit
    override fun onTitleChanged(value: String) = Unit
    override fun onDescriptionChanged(value: String) = Unit
    override fun onTagInputChanged(value: String) = Unit
    override fun onPendingTagSubmitted() = Unit
    override fun onTagSuggestionClicked(tag: String) = Unit
    override fun onTagRemoved(tag: String) = Unit
    override fun onAdultChanged(value: Boolean) = Unit
    override fun onSuggestedImageChanged(index: Int) = Unit
    override fun onPhotoAttachClicked() = Unit
    override fun onPhotoRemoved() = Unit
    override fun onSubmitClicked() = Unit
    override fun onCancelClicked() = Unit
}
