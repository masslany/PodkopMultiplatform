package pl.masslany.podkop.features.resources.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.common.preview.PodkopPreview
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_resource_reply_close
import podkop.composeapp.generated.resources.composer_bold_placeholder
import podkop.composeapp.generated.resources.composer_code_placeholder
import podkop.composeapp.generated.resources.composer_italic_placeholder
import podkop.composeapp.generated.resources.composer_link_description_placeholder
import podkop.composeapp.generated.resources.composer_link_url_placeholder
import podkop.composeapp.generated.resources.composer_quote_placeholder
import podkop.composeapp.generated.resources.entry_details_reply_composer_hint
import podkop.composeapp.generated.resources.entry_details_reply_composer_send
import podkop.composeapp.generated.resources.entry_details_reply_composer_target
import podkop.composeapp.generated.resources.ic_close
import podkop.composeapp.generated.resources.ic_code
import podkop.composeapp.generated.resources.ic_exclamation
import podkop.composeapp.generated.resources.ic_format_bold
import podkop.composeapp.generated.resources.ic_format_italic
import podkop.composeapp.generated.resources.ic_format_quote
import podkop.composeapp.generated.resources.ic_link
import podkop.composeapp.generated.resources.links_screen_label_adult_rating

@Composable
fun ReplyComposer(
    isVisible: Boolean,
    // TODO: Consider TextFieldState & removing Compose class from the state
    content: TextFieldValue,
    isAdult: Boolean,
    hintText: String,
    replyTarget: String?,
    isSubmitting: Boolean,
    onContentChanged: (TextFieldValue) -> Unit,
    onAdultChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isVisible) {
        if (isVisible) {
            focusRequester.requestFocus()
        } else {
            focusManager.clearFocus()
        }
    }

    AnimatedVisibility(
        modifier = modifier.fillMaxWidth(),
        visible = isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    modifier = Modifier
                        .align(Alignment.End),
                    enabled = !isSubmitting,
                    onClick = onDismiss,
                ) {
                    Icon(
                        imageVector = vectorResource(resource = Res.drawable.ic_close),
                        contentDescription = stringResource(
                            resource = Res.string.accessibility_resource_reply_close,
                        ),
                    )
                }

                replyTarget?.let { target ->
                    Text(
                        text = stringResource(
                            resource = Res.string.entry_details_reply_composer_target,
                            target,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val boldPlaceholder = stringResource(resource = Res.string.composer_bold_placeholder)
                    MarkdownActionButton(
                        iconRes = Res.drawable.ic_format_bold,
                        enabled = !isSubmitting,
                        onClick = {
                            val updated = content.insertMarkdown(
                                prefix = "**",
                                placeholder = boldPlaceholder,
                                suffix = "**",
                                highlightPlaceholder = true,
                            )
                            onContentChanged(updated)
                        },
                    )
                    val italicPlaceholder = stringResource(resource = Res.string.composer_italic_placeholder)
                    MarkdownActionButton(
                        iconRes = Res.drawable.ic_format_italic,
                        enabled = !isSubmitting,
                        onClick = {
                            val updated = content.insertMarkdown(
                                prefix = "__",
                                placeholder = italicPlaceholder,
                                suffix = "__",
                                highlightPlaceholder = true,
                            )
                            onContentChanged(updated)
                        },
                    )
                    val linkDescriptionPlaceholder =
                        stringResource(resource = Res.string.composer_link_description_placeholder)
                    val linkUrlPlaceholder = stringResource(resource = Res.string.composer_link_url_placeholder)
                    MarkdownActionButton(
                        iconRes = Res.drawable.ic_link,
                        enabled = !isSubmitting,
                        onClick = {
                            val updated = content.insertMarkdown(
                                prefix = "[$linkDescriptionPlaceholder]($linkUrlPlaceholder)",
                                placeholder = "",
                                suffix = "",
                                highlightPlaceholder = false,
                            )
                            onContentChanged(updated)
                        },
                    )
                    val quotePlaceholder = stringResource(resource = Res.string.composer_quote_placeholder)
                    MarkdownActionButton(
                        iconRes = Res.drawable.ic_format_quote,
                        enabled = !isSubmitting,
                        onClick = {
                            val updated = content.insertMarkdown(
                                prefix = ">",
                                placeholder = quotePlaceholder,
                                suffix = "",
                                highlightPlaceholder = true,
                            )
                            onContentChanged(updated)
                        },
                    )
                    val codePlaceholder = stringResource(resource = Res.string.composer_code_placeholder)
                    MarkdownActionButton(
                        iconRes = Res.drawable.ic_code,
                        enabled = !isSubmitting,
                        onClick = {
                            val updated = content.insertMarkdown(
                                prefix = "`",
                                placeholder = codePlaceholder,
                                suffix = "`",
                                highlightPlaceholder = true,
                            )
                            onContentChanged(updated)
                        },
                    )
                    MarkdownActionButton(
                        iconRes = Res.drawable.ic_exclamation,
                        enabled = !isSubmitting,
                        onClick = {
                            val updated = content.addSpoilerAtLineStart()
                            onContentChanged(updated)
                        },
                    )
                }

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = content,
                    onValueChange = {
                        onContentChanged(it)
                    },
                    enabled = !isSubmitting,
                    minLines = 3,
                    placeholder = {
                        Text(text = hintText)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Switch(
                            checked = isAdult,
                            enabled = !isSubmitting,
                            onCheckedChange = onAdultChanged,
                        )
                        Text(
                            text = stringResource(resource = Res.string.links_screen_label_adult_rating),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Button(
                        onClick = onSubmit,
                        enabled = !isSubmitting && content.text.isNotBlank(),
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = stringResource(resource = Res.string.entry_details_reply_composer_send),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownActionButton(
    iconRes: DrawableResource,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        enabled = enabled,
        onClick = onClick,
    ) {
        Icon(
            imageVector = vectorResource(resource = iconRes),
            contentDescription = null,
        )
    }
}

private fun TextFieldValue.insertMarkdown(
    prefix: String,
    placeholder: String,
    suffix: String,
    highlightPlaceholder: Boolean,
): TextFieldValue {
    val selectionStart = minOf(selection.start, selection.end)
    val selectionEnd = maxOf(selection.start, selection.end)
    val insertion = prefix + placeholder + suffix
    val updatedText = text.replaceRange(selectionStart, selectionEnd, insertion)
    val updatedSelection = if (highlightPlaceholder && placeholder.isNotEmpty()) {
        val highlightStart = selectionStart + prefix.length
        TextRange(start = highlightStart, end = highlightStart + placeholder.length)
    } else {
        TextRange(selectionStart + insertion.length)
    }

    return copy(
        text = updatedText,
        selection = updatedSelection,
        composition = null,
    )
}

private fun TextFieldValue.addSpoilerAtLineStart(): TextFieldValue {
    val selectionStart = minOf(selection.start, selection.end)
    val previousNewLine = if (selectionStart <= 0) {
        -1
    } else {
        text.lastIndexOf('\n', startIndex = selectionStart - 1)
    }
    val lineStart = previousNewLine + 1
    if (text.getOrNull(lineStart) == '!') {
        return this
    }

    val updatedText = text.substring(0, lineStart) + "!" + text.substring(lineStart)
    val updatedSelectionStart = if (selection.start >= lineStart) selection.start + 1 else selection.start
    val updatedSelectionEnd = if (selection.end >= lineStart) selection.end + 1 else selection.end

    return copy(
        text = updatedText,
        selection = TextRange(updatedSelectionStart, updatedSelectionEnd),
        composition = null,
    )
}

@Preview
@Composable
private fun ReplyComposerPreview() {
    PodkopPreview(darkTheme = true) {
        ReplyComposer(
            isVisible = true,
            content = TextFieldValue("test"),
            isAdult = false,
            hintText = stringResource(resource = Res.string.entry_details_reply_composer_hint),
            replyTarget = null,
            isSubmitting = false,
            onContentChanged = {},
            onAdultChanged = {},
            onDismiss = {},
            onSubmit = {},
        )
    }
}
