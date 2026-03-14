package pl.masslany.podkop.features.resourceactions.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotExportAction
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotPreviewDialogState
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotShareDraft
import pl.masslany.podkop.features.resources.preview.EntryCommentItemStateProvider
import pl.masslany.podkop.features.resources.preview.EntryItemStateProvider
import pl.masslany.podkop.features.resources.preview.LinkCommentItemStateProvider

class ResourceScreenshotPreviewDialogStateProvider : PreviewParameterProvider<ResourceScreenshotPreviewDialogState> {
    private val entry = EntryItemStateProvider().values.first()
    private val entryComment = EntryCommentItemStateProvider().values.first()
    private val linkCommentThread = LinkCommentItemStateProvider().values.drop(1).first()
    private val linkReply = linkCommentThread.replies.first()

    override val values: Sequence<ResourceScreenshotPreviewDialogState> = sequenceOf(
        ResourceScreenshotPreviewDialogState(
            draft = ResourceScreenshotShareDraft.Entry(entry = entry),
            showParent = false,
            exportingAction = null,
        ),
        ResourceScreenshotPreviewDialogState(
            draft = ResourceScreenshotShareDraft.EntryComment(
                comment = entryComment,
                parentEntry = entry,
            ),
            showParent = true,
            exportingAction = null,
        ),
        ResourceScreenshotPreviewDialogState(
            draft = ResourceScreenshotShareDraft.LinkComment(
                comment = linkReply,
                parentComment = linkCommentThread,
            ),
            showParent = true,
            exportingAction = ResourceScreenshotExportAction.Share,
        ),
    )
}
