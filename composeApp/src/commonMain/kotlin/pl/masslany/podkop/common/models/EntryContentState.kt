package pl.masslany.podkop.common.models

import com.mikepenz.markdown.model.ReferenceLinkHandlerImpl
import com.mikepenz.markdown.model.State
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

sealed class EntryContentState {

    data class Content(val content: String, val markdownState: State.Success, val isDownVoted: Boolean) :
        EntryContentState()

    data object DeletedByModerator : EntryContentState()

    data object DeletedByAuthor : EntryContentState()

    data object DeletedByEntryAuthor : EntryContentState()
}

fun String.toHighlightedTagProfileMarkdown(): String = this
    .replace(Regex("@[\\w\\d-]+")) {
        "[${it.value}](${it.value})"
    }
    .replace(Regex("#[\\w\\d]+")) {
        "[${it.value}](${it.value})"
    }

fun String.toEntryContentState(isDownVoted: Boolean): EntryContentState.Content {
    val highlightedContent = toHighlightedTagProfileMarkdown()
    return EntryContentState.Content(
        content = highlightedContent,
        markdownState = highlightedContent.toMarkdownStateSuccess(),
        isDownVoted = isDownVoted,
    )
}

fun String.toMarkdownStateSuccess(): State.Success {
    val parser = MarkdownParser(GFMFlavourDescriptor())
    return State.Success(
        node = parser.buildMarkdownTreeFromString(this),
        content = this,
        linksLookedUp = false,
        referenceLinkHandler = ReferenceLinkHandlerImpl(),
    )
}
