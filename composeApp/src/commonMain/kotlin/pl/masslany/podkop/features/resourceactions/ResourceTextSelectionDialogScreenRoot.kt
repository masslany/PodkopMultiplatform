package pl.masslany.podkop.features.resourceactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.common.components.Author
import pl.masslany.podkop.common.components.Avatar
import pl.masslany.podkop.common.components.Published
import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.navigation.SetDialogDestinationToEdgeToEdge
import pl.masslany.podkop.common.platform.rememberPlatformClipboard
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.resourceactions.preview.NoOpResourceTextSelectionDialogActions
import pl.masslany.podkop.features.resourceactions.preview.ResourceTextSelectionDialogStateProvider
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.dialog_button_dismiss
import podkop.composeapp.generated.resources.resource_text_selection_dialog_copy_selection
import podkop.composeapp.generated.resources.resource_text_selection_dialog_hint
import podkop.composeapp.generated.resources.resource_text_selection_dialog_title

@Composable
fun ResourceTextSelectionDialogScreenRoot(
    screen: ResourceTextSelectionDialogScreen,
    modifier: Modifier = Modifier,
) {
    SetDialogDestinationToEdgeToEdge()

    val viewModel = koinViewModel<ResourceTextSelectionDialogViewModel>(
        parameters = {
            parametersOf(
                ResourceTextSelectionDialogParams(
                    content = screen.content,
                    previewDraftId = screen.previewDraftId,
                ),
            )
        },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    ResourceTextSelectionDialogContent(
        state = state,
        actions = viewModel,
        modifier = modifier,
    )
}

@Composable
private fun ResourceTextSelectionDialogContent(
    state: ResourceTextSelectionDialogState,
    actions: ResourceTextSelectionDialogActions,
    modifier: Modifier = Modifier,
) {
    val clipboard = rememberPlatformClipboard()
    val coroutineScope = rememberCoroutineScope()
    val dismissInteractionSource = remember { MutableInteractionSource() }
    val contentInteractionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.56f))
                .clickable(
                    interactionSource = dismissInteractionSource,
                    indication = null,
                    onClick = { actions.onDismissClicked() },
                ),
        )

        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .clickable(
                    interactionSource = contentInteractionSource,
                    indication = null,
                    onClick = {},
                ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (val previewDraft = state.previewDraft) {
                    null -> {
                        Text(
                            text = stringResource(resource = Res.string.resource_text_selection_dialog_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    is ResourceScreenshotShareDraft.Entry -> {
                        ResourceTextSelectionPreviewHeader(
                            avatarState = previewDraft.entry.avatarState,
                            authorState = previewDraft.entry.authorState,
                            publishedTimeType = previewDraft.entry.publishedTimeType,
                        )
                    }

                    is ResourceScreenshotShareDraft.EntryComment -> {
                        ResourceTextSelectionPreviewHeader(
                            avatarState = previewDraft.comment.avatarState,
                            authorState = previewDraft.comment.authorState,
                            publishedTimeType = previewDraft.comment.publishedTimeType,
                        )
                    }

                    is ResourceScreenshotShareDraft.LinkComment -> {
                        ResourceTextSelectionPreviewHeader(
                            avatarState = previewDraft.comment.avatarState,
                            authorState = previewDraft.comment.authorState,
                            publishedTimeType = previewDraft.comment.publishedTimeType,
                        )
                    }
                }

                Text(
                    text = stringResource(resource = Res.string.resource_text_selection_dialog_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                ResourceTextSelectionField(
                    value = state.content,
                    onValueChange = { content -> actions.onTextChanged(content) },
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { actions.onDismissClicked() },
                    ) {
                        Text(text = stringResource(resource = Res.string.dialog_button_dismiss))
                    }

                    Spacer(Modifier.size(8.dp))

                    Button(
                        onClick = {
                            val selectedText = state.content.selectedText()
                            if (selectedText.isEmpty()) {
                                return@Button
                            }

                            coroutineScope.launch {
                                clipboard.setText(selectedText)
                                actions.onCopySelectionCompleted()
                            }
                        },
                        enabled = state.hasSelection,
                    ) {
                        Text(
                            text = stringResource(
                                resource = Res.string.resource_text_selection_dialog_copy_selection,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResourceTextSelectionPreviewHeader(
    avatarState: AvatarState,
    authorState: AuthorState?,
    publishedTimeType: PublishedTimeType?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            state = avatarState,
            onClick = {},
        )
        Spacer(Modifier.size(8.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            authorState?.let {
                Author(
                    state = it,
                    onClick = {},
                )
            }
            publishedTimeType?.let {
                Published(type = it)
            }
        }
    }
}

@Composable
private fun ResourceTextSelectionField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
) {
    BasicTextField(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .heightIn(min = 120.dp, max = 320.dp)
            .verticalScroll(rememberScrollState()),
        value = value,
        onValueChange = onValueChange,
        readOnly = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        minLines = 4,
        cursorBrush = SolidColor(Color.Unspecified),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.wrapContentHeight(),
            ) {
                innerTextField()
            }
        },
    )
}

private fun TextFieldValue.selectedText(): String {
    if (selection.collapsed) return ""

    val start = selection.min.coerceAtLeast(0)
    val end = selection.max.coerceAtMost(text.length)
    if (start >= end) return ""

    return text.substring(startIndex = start, endIndex = end)
}

@Preview
@Composable
private fun ResourceTextSelectionDialogContentPreview(
    @PreviewParameter(ResourceTextSelectionDialogStateProvider::class) state: ResourceTextSelectionDialogState,
) {
    PodkopPreview(darkTheme = false) {
        ResourceTextSelectionDialogContent(
            state = state,
            actions = NoOpResourceTextSelectionDialogActions,
        )
    }
}
