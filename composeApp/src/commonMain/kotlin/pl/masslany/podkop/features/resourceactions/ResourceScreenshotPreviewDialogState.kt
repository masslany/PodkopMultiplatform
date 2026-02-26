package pl.masslany.podkop.features.resourceactions

data class ResourceScreenshotPreviewDialogState(
    val draft: ResourceScreenshotShareDraft?,
    val showParent: Boolean,
    val exportingAction: ResourceScreenshotExportAction?,
) {
    companion object {
        val initial = ResourceScreenshotPreviewDialogState(
            draft = null,
            showParent = false,
            exportingAction = null,
        )
    }

    val isExporting: Boolean
        get() = exportingAction != null

    val isParentToggleVisible: Boolean
        get() = when (val currentDraft = draft) {
            is ResourceScreenshotShareDraft.EntryComment -> currentDraft.parentEntry != null

            is ResourceScreenshotShareDraft.LinkComment -> currentDraft.parentComment != null

            is ResourceScreenshotShareDraft.Entry,
            null,
            -> false
        }
}
