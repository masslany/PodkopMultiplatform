package pl.masslany.podkop.features.resourceactions

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ResourceActionsBottomSheetState(val actions: ImmutableList<ResourceActionItemState> = persistentListOf()) {
    companion object {
        val initial = ResourceActionsBottomSheetState()
    }
}

data class ResourceActionItemState(
    val id: ResourceActionId,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val localAction: ResourceActionLocalAction? = null,
    val isDestructive: Boolean = false,
)

enum class ResourceActionId {
    CopyText,
    SelectText,
    CopyAsLink,
    Report,
    ShareAsScreenshot,
    ShowVoters,
    ShowLinkUpvoters,
    ShowLinkDownvoters,
    DeleteEntry,
    DeleteEntryComment,
    EditEntry,
    EditEntryComment,
    EditLinkComment,
}

sealed interface ResourceActionLocalAction {
    data class CopyToClipboard(val value: String) : ResourceActionLocalAction
}

data class ResourceActionsParams(
    val resourceType: ResourceActionsType,
    val rootId: Int,
    val rootSlug: String? = null,
    val parentId: Int? = null,
    val childId: Int? = null,
    val screenshotDraftId: String? = null,
    val canDelete: Boolean = false,
    val canEdit: Boolean = false,
    val content: String = "",
    val copyContent: String? = null,
    val adult: Boolean = false,
    val photoKey: String? = null,
    val photoUrl: String? = null,
)
