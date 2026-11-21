package pl.masslany.podkop.features.resources

import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.resourceactions.ResourceActionsBottomSheetScreen
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotShareDraft
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotShareDraftStore
import pl.masslany.podkop.features.resourceactions.ResourceTextSelectionDialogScreen
import pl.masslany.podkop.features.resources.models.entry.EntryItemState
import pl.masslany.podkop.features.resources.models.entrycomment.EntryCommentItemState
import pl.masslany.podkop.features.resources.models.linkcomment.LinkCommentItemState

/**
 * Opens an item's actions sheet ("more") and its text selection dialog (long press). Both get a
 * screenshot draft of the item, so they can share it as an image.
 */
internal class ResourceItemMenus(
    private val store: ResourceItemsStore,
    private val appNavigator: AppNavigator,
    private val screenshotShareDraftStore: ResourceScreenshotShareDraftStore,
) {
    fun openEntryActions(entryId: Int) {
        val entry = store.findEntry(entryId)
        appNavigator.navigateTo(
            ResourceActionsBottomSheetScreen.forEntry(
                entryId = entryId,
                screenshotDraftId = entry?.let(::putEntryDraft),
                canDelete = entry?.isDeleteEnabled == true,
                canEdit = entry?.isEditEnabled == true,
                content = entry?.rawContent.orEmpty(),
                copyContent = entry?.copyableContent,
                adult = entry?.adult == true,
                photoKey = entry?.photoKey,
                photoUrl = entry?.photoUrl,
            ),
        )
    }

    fun openEntryTextSelection(entryId: Int) {
        val entry = store.findEntry(entryId) ?: return
        openTextSelection(
            content = entry.copyableContent ?: return,
            previewDraftId = putEntryDraft(entry),
        )
    }

    fun openEntryCommentActions(entryId: Int, entryCommentId: Int) {
        val comment = store.findEntryComment(entryId, entryCommentId)
        appNavigator.navigateTo(
            ResourceActionsBottomSheetScreen.forEntryComment(
                entryId = entryId,
                entryCommentId = entryCommentId,
                screenshotDraftId = comment?.let { putEntryCommentDraft(entryId, it) },
                canDelete = comment?.isDeleteEnabled == true,
                canEdit = comment?.isEditEnabled == true,
                content = comment?.rawContent.orEmpty(),
                copyContent = comment?.copyableContent,
                adult = comment?.adult == true,
                photoKey = comment?.embedImageState?.key,
                photoUrl = comment?.embedImageState?.url,
            ),
        )
    }

    fun openEntryCommentTextSelection(entryId: Int, entryCommentId: Int) {
        val comment = store.findEntryComment(entryId, entryCommentId) ?: return
        openTextSelection(
            content = comment.copyableContent ?: return,
            previewDraftId = putEntryCommentDraft(entryId, comment),
        )
    }

    fun openLinkCommentActions(linkId: Int, commentId: Int, linkSlug: String, parentCommentId: Int?) {
        val comment = store.findLinkComment(commentId)
        appNavigator.navigateTo(
            ResourceActionsBottomSheetScreen.forLinkComment(
                linkId = linkId,
                linkSlug = linkSlug,
                linkCommentId = commentId,
                parentCommentId = parentCommentId,
                screenshotDraftId = comment?.let(::putLinkCommentDraft),
                canEdit = comment?.isEditEnabled == true,
                content = comment?.rawContent.orEmpty(),
                copyContent = comment?.copyableContent,
                adult = comment?.adult == true,
                photoKey = comment?.embedImageState?.key,
                photoUrl = comment?.embedImageState?.url,
            ),
        )
    }

    fun openLinkCommentTextSelection(commentId: Int) {
        val comment = store.findLinkComment(commentId) ?: return
        openTextSelection(
            content = comment.copyableContent ?: return,
            previewDraftId = putLinkCommentDraft(comment),
        )
    }

    private fun openTextSelection(content: String, previewDraftId: String) {
        appNavigator.navigateTo(
            ResourceTextSelectionDialogScreen(
                content = content,
                previewDraftId = previewDraftId,
            ),
        )
    }

    private fun putEntryDraft(entry: EntryItemState): String =
        screenshotShareDraftStore.put(ResourceScreenshotShareDraft.Entry(entry = entry))

    private fun putEntryCommentDraft(entryId: Int, comment: EntryCommentItemState): String =
        screenshotShareDraftStore.put(
            ResourceScreenshotShareDraft.EntryComment(
                comment = comment,
                parentEntry = store.findEntry(entryId),
            ),
        )

    private fun putLinkCommentDraft(comment: LinkCommentItemState): String =
        screenshotShareDraftStore.put(ResourceScreenshotShareDraft.LinkComment(comment = comment))
}

private val EntryItemState.copyableContent: String?
    get() = copyableContent(rawContent, entryContentState, isBlacklisted)

private val EntryCommentItemState.copyableContent: String?
    get() = copyableContent(rawContent, entryContentState, isBlacklisted)

private val LinkCommentItemState.copyableContent: String?
    get() = copyableContent(rawContent, entryContentState, isBlacklisted)

private fun copyableContent(
    rawContent: String,
    entryContentState: EntryContentState,
    isBlacklisted: Boolean,
): String? = rawContent.takeIf {
    entryContentState is EntryContentState.Content && !isBlacklisted && it.isNotBlank()
}
