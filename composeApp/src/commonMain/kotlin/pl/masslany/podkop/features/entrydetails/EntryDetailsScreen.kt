package pl.masslany.podkop.features.entrydetails

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class EntryDetailsScreen(val id: Int, val pendingComposerIntent: EntryComposerIntent? = null) : NavTarget {
    companion object {
        fun forEntry(id: Int): EntryDetailsScreen = EntryDetailsScreen(id = id)

        fun forEntryReply(entryId: Int, author: String?): EntryDetailsScreen = EntryDetailsScreen(
            id = entryId,
            pendingComposerIntent = EntryComposerIntent.reply(author = author),
        )

        fun forEntryCommentReply(entryId: Int, entryCommentId: Int, author: String?): EntryDetailsScreen =
            EntryDetailsScreen(
                id = entryId,
                pendingComposerIntent = EntryComposerIntent.reply(
                    author = author,
                    entryCommentId = entryCommentId,
                ),
            )
    }
}

@Serializable
data class EntryComposerIntent(
    val type: EntryComposerIntentType,
    val author: String? = null,
    val entryCommentId: Int? = null,
) {
    companion object {
        fun reply(author: String?, entryCommentId: Int? = null): EntryComposerIntent = EntryComposerIntent(
            type = EntryComposerIntentType.Reply,
            author = author,
            entryCommentId = entryCommentId,
        )
    }
}

@Serializable
enum class EntryComposerIntentType {
    Reply,
}
