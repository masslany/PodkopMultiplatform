package pl.masslany.podkop.features.resourceactions

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
enum class ResourceVotesType {
    Entry,
    EntryComment,
}

@Serializable
data class ResourceVotesBottomSheetScreen(
    val resourceType: ResourceVotesType,
    val entryId: Int,
    val entryCommentId: Int? = null,
) : NavTarget {
    init {
        require(resourceType == ResourceVotesType.Entry || entryCommentId != null) {
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
    }
}
