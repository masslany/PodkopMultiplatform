package pl.masslany.podkop.features.resourceactions

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
enum class ResourceActionsType {
    Link,
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
    val screenshotDraftId: String? = null,
    val canDelete: Boolean = false,
    val canEdit: Boolean = false,
    val content: String = "",
    val adult: Boolean = false,
    val photoKey: String? = null,
    val photoUrl: String? = null,
) : NavTarget {
    init {
        require(
            resourceType == ResourceActionsType.Entry ||
                resourceType == ResourceActionsType.Link ||
                childId != null,
        ) {
            "Comment actions require childId"
        }
        require(resourceType != ResourceActionsType.Link || !rootSlug.isNullOrBlank()) {
            "Link actions require rootSlug"
        }
        require(resourceType != ResourceActionsType.LinkComment || !rootSlug.isNullOrBlank()) {
            "Link comment actions require rootSlug"
        }
    }

    companion object {
        fun forEntry(
            entryId: Int,
            screenshotDraftId: String? = null,
            canDelete: Boolean = false,
            canEdit: Boolean = false,
            content: String = "",
            adult: Boolean = false,
            photoKey: String? = null,
            photoUrl: String? = null,
        ): ResourceActionsBottomSheetScreen = ResourceActionsBottomSheetScreen(
            resourceType = ResourceActionsType.Entry,
            rootId = entryId,
            screenshotDraftId = screenshotDraftId,
            canDelete = canDelete,
            canEdit = canEdit,
            content = content,
            adult = adult,
            photoKey = photoKey,
            photoUrl = photoUrl,
        )

        fun forLink(
            linkId: Int,
            linkSlug: String,
        ): ResourceActionsBottomSheetScreen = ResourceActionsBottomSheetScreen(
            resourceType = ResourceActionsType.Link,
            rootId = linkId,
            rootSlug = linkSlug,
        )

        fun forEntryComment(
            entryId: Int,
            entryCommentId: Int,
            screenshotDraftId: String? = null,
            canDelete: Boolean = false,
            canEdit: Boolean = false,
            content: String = "",
            adult: Boolean = false,
            photoKey: String? = null,
            photoUrl: String? = null,
        ): ResourceActionsBottomSheetScreen = ResourceActionsBottomSheetScreen(
            resourceType = ResourceActionsType.EntryComment,
            rootId = entryId,
            childId = entryCommentId,
            screenshotDraftId = screenshotDraftId,
            canDelete = canDelete,
            canEdit = canEdit,
            content = content,
            adult = adult,
            photoKey = photoKey,
            photoUrl = photoUrl,
        )

        fun forLinkComment(
            linkId: Int,
            linkSlug: String,
            linkCommentId: Int,
            parentCommentId: Int?,
            screenshotDraftId: String? = null,
            canEdit: Boolean = false,
            content: String = "",
            adult: Boolean = false,
            photoKey: String? = null,
            photoUrl: String? = null,
        ): ResourceActionsBottomSheetScreen = ResourceActionsBottomSheetScreen(
            resourceType = ResourceActionsType.LinkComment,
            rootId = linkId,
            rootSlug = linkSlug,
            parentId = parentCommentId,
            childId = linkCommentId,
            screenshotDraftId = screenshotDraftId,
            canEdit = canEdit,
            content = content,
            adult = adult,
            photoKey = photoKey,
            photoUrl = photoUrl,
        )
    }
}
