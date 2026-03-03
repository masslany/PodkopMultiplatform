package pl.masslany.podkop.features.resourceactions

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
enum class ResourceVotesType {
    Entry,
    EntryComment,
    LinkUp,
    LinkDown,
}

@Serializable
data class ResourceVotesBottomSheetScreen(
    val resourceType: ResourceVotesType,
    val entryId: Int = 0,
    val entryCommentId: Int? = null,
    val linkId: Int = 0,
) : NavTarget {
    init {
        require(
            resourceType == ResourceVotesType.Entry ||
                resourceType == ResourceVotesType.LinkUp ||
                resourceType == ResourceVotesType.LinkDown ||
                entryCommentId != null,
        ) {
            "Entry comment votes require entryCommentId"
        }
    }

    companion object {
        fun forEntry(entryId: Int): ResourceVotesBottomSheetScreen = ResourceVotesBottomSheetScreen(
            resourceType = ResourceVotesType.Entry,
            entryId = entryId,
        )

        fun forEntryComment(
            entryId: Int,
            entryCommentId: Int,
        ): ResourceVotesBottomSheetScreen = ResourceVotesBottomSheetScreen(
            resourceType = ResourceVotesType.EntryComment,
            entryId = entryId,
            entryCommentId = entryCommentId,
        )

        fun forLinkUpvotes(linkId: Int): ResourceVotesBottomSheetScreen = ResourceVotesBottomSheetScreen(
            resourceType = ResourceVotesType.LinkUp,
            linkId = linkId,
        )

        fun forLinkDownvotes(linkId: Int): ResourceVotesBottomSheetScreen = ResourceVotesBottomSheetScreen(
            resourceType = ResourceVotesType.LinkDown,
            linkId = linkId,
        )
    }
}
