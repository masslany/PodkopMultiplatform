package pl.masslany.podkop.common.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.annotator.DefaultAnnotatorSettings
import com.mikepenz.markdown.annotator.buildMarkdownAnnotatedString
import com.mikepenz.markdown.compose.LocalMarkdownAnnotator
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownText
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.utils.codeSpanStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.theme.colorsPalette
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.comment_button_show_spoiler
import podkop.composeapp.generated.resources.comment_label_removed_by_author
import podkop.composeapp.generated.resources.comment_label_removed_by_moderator

@Composable
fun EntryContent(state: EntryContentState) {
    when (state) {
        is EntryContentState.Content -> {
            if (state.content.isNotEmpty()) {
                val markdownState = rememberCachedEntryMarkdownState(state.content)
                Markdown(
                    state = markdownState ?: State.Loading(),
                    modifier = Modifier.fillMaxWidth(),
                    components = spoilerComponents,
                    colors = markdownColor(
                        text = MaterialTheme.colorScheme.onSurface,
                    ),
                    typography = markdownTypography(
                        text = MaterialTheme.typography.bodySmall,
                        paragraph = MaterialTheme.typography.bodySmall,
                        textLink = TextLinkStyles(
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorsPalette.tagBlue,
                            ).toSpanStyle(),
                        ),
                    ),
                    animations = markdownAnimations(
                        animateTextSize = { this },
                    ),
                )
            }
        }

        EntryContentState.DeletedByAuthor -> {
            Text(
                text = stringResource(resource = Res.string.comment_label_removed_by_author),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }

        EntryContentState.DeletedByModerator -> {
            Text(
                text = stringResource(resource = Res.string.comment_label_removed_by_moderator),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
private fun rememberCachedEntryMarkdownState(content: String): State.Success? {
    val cache = LocalMarkdownStateCache.current
    return produceState<State.Success?>(
        initialValue = null,
        key1 = content,
        key2 = cache,
    ) {
        value = cache.get(content)
        if (value == null) {
            value = withContext(Dispatchers.Default) { cache.getOrParse(content) }
        }
    }.value
}

private val spoilerComponents = markdownComponents(
    paragraph = { config ->
        val content = config.content
        val node = config.node
        val typography = config.typography
        val annotator = LocalMarkdownAnnotator.current
        val codeSpanStyle = typography.codeSpanStyle

        val isSpoiler = content.getOrNull(node.startOffset) == '!'

        if (isSpoiler) {
            var revealed by remember(content, node.startOffset) { mutableStateOf(false) }

            if (revealed) {
                // Build annotated content once and remove the leading "!" marker.
                val strippedText = remember(
                    content,
                    node.startOffset,
                    node.endOffset,
                    typography.paragraph,
                    typography.textLink,
                    codeSpanStyle,
                    annotator,
                ) {
                    val settings = DefaultAnnotatorSettings(
                        annotator = annotator,
                        linkTextSpanStyle = typography.textLink,
                        codeSpanStyle = codeSpanStyle,
                    )

                    val fullAnnotatedString = content.buildMarkdownAnnotatedString(
                        textNode = node,
                        style = typography.paragraph,
                        annotatorSettings = settings,
                    )

                    if (fullAnnotatedString.length > 1) {
                        fullAnnotatedString.subSequence(1, fullAnnotatedString.length)
                    } else {
                        fullAnnotatedString
                    }
                }

                MarkdownText(
                    content = strippedText,
                )
            } else {
                Button(
                    modifier = Modifier.height(24.dp),
                    onClick = { revealed = true },
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),

                ) {
                    Text(
                        text = stringResource(resource = Res.string.comment_button_show_spoiler),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        } else {
            MarkdownText(
                content = content,
                node = node,
                style = typography.paragraph,
            )
        }
    },
)
