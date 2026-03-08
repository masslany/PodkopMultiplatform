package pl.masslany.podkop.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.annotator.DefaultAnnotatorSettings
import com.mikepenz.markdown.annotator.buildMarkdownAnnotatedString
import com.mikepenz.markdown.compose.LocalMarkdownAnnotator
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownPadding
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownListItems
import com.mikepenz.markdown.compose.elements.MarkdownText
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.markdownPadding
import com.mikepenz.markdown.utils.codeSpanStyle
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import org.intellij.markdown.MarkdownTokenTypes.Companion.LIST_BULLET
import org.intellij.markdown.MarkdownTokenTypes.Companion.LIST_NUMBER
import org.intellij.markdown.ast.ASTNode
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.models.EntryContentState
import pl.masslany.podkop.common.models.toEntryContentState
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.preview.PreviewFixtures
import pl.masslany.podkop.common.theme.colorsPalette
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.comment_button_show_spoiler
import podkop.composeapp.generated.resources.comment_label_removed_by_author
import podkop.composeapp.generated.resources.comment_label_removed_by_moderator

@Composable
fun EntryContent(
    state: EntryContentState,
    onProfileClick: (String) -> Unit,
    onTagClick: (String) -> Unit,
    onUrlClick: (String) -> Unit,
) {
    val profileClickHandler by rememberUpdatedState(onProfileClick)
    val tagClickHandler by rememberUpdatedState(onTagClick)
    val urlClickHandler by rememberUpdatedState(onUrlClick)
    val uriHandler = remember {
        object : UriHandler {
            override fun openUri(uri: String) {
                when {
                    uri.startsWith("@") -> {
                        uri.removePrefix("@")
                            .takeIf(String::isNotBlank)
                            ?.let(profileClickHandler)
                            ?: urlClickHandler(uri)
                    }

                    uri.startsWith("#") -> {
                        uri.removePrefix("#")
                            .takeIf(String::isNotBlank)
                            ?.let(tagClickHandler)
                            ?: urlClickHandler(uri)
                    }

                    else -> urlClickHandler(uri)
                }
            }
        }
    }

    when (state) {
        is EntryContentState.Content -> {
            if (state.content.isNotEmpty()) {
                CompositionLocalProvider(LocalUriHandler provides uriHandler) {
                    Markdown(
                        state = state.markdownState,
                        modifier = Modifier.fillMaxWidth(),
                        components = markdownComponents,
                        colors = markdownColor(
                            text = if (state.isDownVoted) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
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

private val markdownComponents = markdownComponents(
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
    orderedList = { config ->
        ParagraphLikeMarkdownList(
            content = config.content,
            node = config.node,
            markerForItem = { markerNode ->
                markerNode
                    .takeIf { it.type == LIST_NUMBER }
                    ?.getUnescapedTextInNode(config.content)
                    .orEmpty()
            },
        )
    },
    unorderedList = { config ->
        ParagraphLikeMarkdownList(
            content = config.content,
            node = config.node,
            markerForItem = { markerNode ->
                markerNode
                    .takeIf { it.type == LIST_BULLET }
                    ?.getUnescapedTextInNode(config.content)
                    .orEmpty()
            },
        )
    },
)

@Composable
private fun ParagraphLikeMarkdownList(
    content: String,
    node: ASTNode,
    markerForItem: (ASTNode) -> String,
) {
    val typography = LocalMarkdownTypography.current
    val markdownColors = LocalMarkdownColors.current
    val paragraphLikeListPadding = markdownPadding(
        list = 0.dp,
        listItemTop = 0.dp,
        listItemBottom = 0.dp,
        listIndent = 0.dp,
    )

    CompositionLocalProvider(LocalMarkdownPadding provides paragraphLikeListPadding) {
        MarkdownListItems(
            content = content,
            node = node,
            markerModifier = {
                Modifier.padding(end = 4.dp)
            },
            listModifier = {
                Modifier.weight(1f)
            },
        ) { _, _, listMarker ->
            Text(
                text = listMarker?.let(markerForItem).orEmpty(),
                style = typography.paragraph,
                color = markdownColors.text,
            )
        }
    }
}

@Preview
@Composable
private fun EntryContentPreviewContent() {
    PodkopPreview(darkTheme = false) {
        Column {
            EntryContent(
                state = PreviewFixtures.LONG_BODY.toEntryContentState(isDownVoted = false),
                onProfileClick = {},
                onTagClick = {},
                onUrlClick = {},
            )
            EntryContent(
                state = PreviewFixtures.LONG_BODY.toEntryContentState(isDownVoted = true),
                onProfileClick = {},
                onTagClick = {},
                onUrlClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun EntryContentPreviewDeleted() {
    PodkopPreview(darkTheme = true) {
        EntryContent(
            state = EntryContentState.DeletedByModerator,
            onProfileClick = {},
            onTagClick = {},
            onUrlClick = {},
        )
    }
}
