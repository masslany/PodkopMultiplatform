package pl.masslany.podkop.features.resourceactions

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

data class ResourceActionsBottomSheetState(val actions: ImmutableList<ResourceActionItemState> = persistentListOf()) {
    companion object {
        val initial = ResourceActionsBottomSheetState()
    }
}

data class ResourceActionItemState(
    val id: ResourceActionId,
    val title: StringResource,
    val icon: DrawableResource,
    val localAction: ResourceActionLocalAction? = null,
)

enum class ResourceActionId {
    CopyAsLink,
    ShareAsScreenshot,
    ShowVoters,
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
)
