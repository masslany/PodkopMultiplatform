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

private val separatorOnlyDashLineRegex = Regex("(?m)^([ \\t]*)(-{3,})([ \\t]*)$")

fun String.toHighlightedTagProfileMarkdown(): String = this
    .replace(Regex("@[\\w\\d-]+")) {
        "[${it.value}](${it.value})"
    }
    .replace(Regex("#[\\w\\d]+")) {
        "[${it.value}](${it.value})"
    }

/**
 * Keeps separator lines like `-------------` literal.
 *
 * Without escaping, the Markdown parser can reinterpret those lines as structural Markdown
 * and promote the surrounding text into a heading or divider instead of showing the dashes.
 */
fun String.escapeStandaloneDashSeparators(): String =
    replace(separatorOnlyDashLineRegex) { match ->
        "${match.groupValues[1]}\\${match.groupValues[2]}${match.groupValues[3]}"
    }

fun String.toEntryContentState(isDownVoted: Boolean): EntryContentState.Content {
    val highlightedContent = toHighlightedTagProfileMarkdown()
        .escapeStandaloneDashSeparators()
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
