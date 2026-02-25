package pl.masslany.podkop.features.resourceactions

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
enum class ResourceActionsType {
    Entry,
    EntryComment,
    LinkComment,
}

@Serializable
data class ResourceActionsBottomSheetScreen(
    val resourceType: ResourceActionsType,
    val rootId: Int,
    val rootSlug: String? = null,
    val parentId: Int? = null,
    val childId: Int? = null,
) : NavTarget {
    init {
        require(resourceType == ResourceActionsType.Entry || childId != null) {
            "Comment actions require childId"
        }
        require(resourceType != ResourceActionsType.LinkComment || !rootSlug.isNullOrBlank()) {
            "Link comment actions require rootSlug"
        }
    }

    companion object {
        fun forEntry(entryId: Int): ResourceActionsBottomSheetScreen = ResourceActionsBottomSheetScreen(
            resourceType = ResourceActionsType.Entry,
            rootId = entryId,
        )

        fun forEntryComment(
            entryId: Int,
            entryCommentId: Int,
        ): ResourceActionsBottomSheetScreen = ResourceActionsBottomSheetScreen(
            resourceType = ResourceActionsType.EntryComment,
            rootId = entryId,
            childId = entryCommentId,
        )

        fun forLinkComment(
            linkId: Int,
            linkSlug: String,
            linkCommentId: Int,
            parentCommentId: Int?,
        ): ResourceActionsBottomSheetScreen = ResourceActionsBottomSheetScreen(
            resourceType = ResourceActionsType.LinkComment,
            rootId = linkId,
            rootSlug = linkSlug,
            parentId = parentCommentId,
            childId = linkCommentId,
        )
    }
}
