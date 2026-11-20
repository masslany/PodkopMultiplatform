package pl.masslany.podkop.features.resourceactions.preview

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import pl.masslany.podkop.common.preview.PreviewFixtures
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotShareDraft
import pl.masslany.podkop.features.resourceactions.ResourceTextSelectionDialogState
import pl.masslany.podkop.features.resources.preview.EntryCommentItemStateProvider
import pl.masslany.podkop.features.resources.preview.EntryItemStateProvider
import pl.masslany.podkop.features.resources.preview.LinkCommentItemStateProvider

class ResourceTextSelectionDialogStateProvider : PreviewParameterProvider<ResourceTextSelectionDialogState> {
    private val entryItem = EntryItemStateProvider().values.first()
    private val entryComment = EntryCommentItemStateProvider().values.first()
    private val linkComment = LinkCommentItemStateProvider().values.first()

    override val values: Sequence<ResourceTextSelectionDialogState> = sequenceOf(
        ResourceTextSelectionDialogState(
            content = TextFieldValue(
                text = PreviewFixtures.LONG_BODY,
            ),
        ),
        ResourceTextSelectionDialogState(
            content = TextFieldValue(
                text = entryComment.rawContent,
                selection = TextRange(5, 32),
            ),
            previewDraft = ResourceScreenshotShareDraft.EntryComment(
                comment = entryComment,
                parentEntry = entryItem,
            ),
        ),
        ResourceTextSelectionDialogState(
            content = TextFieldValue(
                text = linkComment.rawContent,
                selection = TextRange(0, 18),
            ),
            previewDraft = ResourceScreenshotShareDraft.LinkComment(
                comment = linkComment,
            ),
        ),
    )
}
