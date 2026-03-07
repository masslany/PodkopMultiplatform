package pl.masslany.podkop.features.resourceactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toIntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import pl.masslany.podkop.features.resources.components.EntryCommentItem
import pl.masslany.podkop.features.resources.components.EntryItem
import pl.masslany.podkop.features.resources.components.LinkCommentItem
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_copy
import podkop.composeapp.generated.resources.ic_download
import podkop.composeapp.generated.resources.ic_share
import podkop.composeapp.generated.resources.screenshot_preview_action_cancel
import podkop.composeapp.generated.resources.screenshot_preview_action_copy
import podkop.composeapp.generated.resources.screenshot_preview_action_save
import podkop.composeapp.generated.resources.screenshot_preview_action_share
import podkop.composeapp.generated.resources.screenshot_preview_show_parent
import podkop.composeapp.generated.resources.screenshot_preview_title

@Composable
fun ResourceScreenshotPreviewDialogScreenRoot(
    screen: ResourceScreenshotPreviewDialogScreen,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<ResourceScreenshotPreviewDialogViewModel>(
        parameters = { parametersOf(screen.draftId) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collect { event ->
            snackbarHostState.showSnackbar(
                message = event.message.resolveDialogSnackbarMessage(),
                actionLabel = event.actionLabel?.resolveDialogSnackbarMessage(),
                withDismissAction = event.withDismissAction,
                duration = if (event.isFinite) SnackbarDuration.Short else SnackbarDuration.Indefinite,
            )
        }
    }

    ResourceScreenshotPreviewDialogContent(
        state = state,
        actions = viewModel,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
private fun ResourceScreenshotPreviewDialogContent(
    state: ResourceScreenshotPreviewDialogState,
    actions: ResourceScreenshotPreviewDialogActions,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val draft = state.draft ?: return
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.screenshot_preview_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    TextButton(
                        onClick = actions::onCancelClicked,
                        enabled = !state.isExporting,
                    ) {
                        Text(text = stringResource(Res.string.screenshot_preview_action_cancel))
                    }
                }

                if (state.isParentToggleVisible) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.screenshot_preview_show_parent),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Switch(
                            checked = state.showParent,
                            onCheckedChange = actions::onShowParentChanged,
                            enabled = !state.isExporting,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                                shape = RoundedCornerShape(20.dp),
                            )
                            .padding(12.dp)
                            .drawWithContent {
                                graphicsLayer.record(size = size.toIntSize()) {
                                    this@drawWithContent.drawContent()
                                }
                                drawLayer(graphicsLayer)
                            },
                    ) {
                        ResourceScreenshotPreviewContent(
                            draft = draft,
                            showParent = state.showParent,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val actionButtonShape = RoundedCornerShape(999.dp)

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        shape = actionButtonShape,
                        onClick = {
                            if (state.isExporting) return@Button
                            scope.launch {
                                val image = graphicsLayer.toImageBitmap()
                                actions.onScreenshotCaptured(
                                    image = image,
                                    action = ResourceScreenshotExportAction.Share,
                                )
                            }
                        },
                        enabled = !state.isExporting,
                    ) {
                        ExportActionLabel(
                            icon = Res.drawable.ic_share,
                            isLoading = state.exportingAction == ResourceScreenshotExportAction.Share,
                            text = stringResource(Res.string.screenshot_preview_action_share),
                        )
                    }

                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val secondaryButtonWidth = (maxWidth - 12.dp) / 2

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedButton(
                                modifier = Modifier.width(secondaryButtonWidth),
                                shape = actionButtonShape,
                                onClick = {
                                    if (state.isExporting) return@OutlinedButton
                                    scope.launch {
                                        val image = graphicsLayer.toImageBitmap()
                                        actions.onScreenshotCaptured(
                                            image = image,
                                            action = ResourceScreenshotExportAction.Copy,
                                        )
                                    }
                                },
                                enabled = !state.isExporting,
                            ) {
                                ExportActionLabel(
                                    icon = Res.drawable.ic_copy,
                                    isLoading = state.exportingAction == ResourceScreenshotExportAction.Copy,
                                    text = stringResource(Res.string.screenshot_preview_action_copy),
                                )
                            }

                            OutlinedButton(
                                modifier = Modifier.width(secondaryButtonWidth),
                                shape = actionButtonShape,
                                onClick = {
                                    if (state.isExporting) return@OutlinedButton
                                    scope.launch {
                                        val image = graphicsLayer.toImageBitmap()
                                        actions.onScreenshotCaptured(
                                            image = image,
                                            action = ResourceScreenshotExportAction.Save,
                                        )
                                    }
                                },
                                enabled = !state.isExporting,
                            ) {
                                ExportActionLabel(
                                    icon = Res.drawable.ic_download,
                                    isLoading = state.exportingAction == ResourceScreenshotExportAction.Save,
                                    text = stringResource(Res.string.screenshot_preview_action_save),
                                )
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .widthIn(max = 640.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun ExportActionLabel(
    icon: DrawableResource,
    isLoading: Boolean,
    text: String,
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
        )
    } else {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
    }
    Spacer(Modifier.size(8.dp))
    Text(text = text)
}

@Composable
private fun ResourceScreenshotPreviewContent(
    draft: ResourceScreenshotShareDraft,
    showParent: Boolean,
) {
    when (draft) {
        is ResourceScreenshotShareDraft.Entry -> {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                EntryItem(
                    modifier = Modifier.padding(16.dp),
                    state = draft.entry,
                    onProfileClick = {},
                    onTagClick = {},
                    onUrlClick = {},
                    onVoteUpClick = {},
                    onFavouriteClick = {},
                    onImageClick = {},
                    onEmbedPreviewClick = {},
                    onMoreClick = {},
                    showInlineActions = false,
                )
            }
        }

        is ResourceScreenshotShareDraft.EntryComment -> {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    if (showParent && draft.parentEntry != null) {
                        EntryItem(
                            modifier = Modifier.fillMaxWidth(),
                            state = draft.parentEntry,
                            onProfileClick = {},
                            onTagClick = {},
                            onUrlClick = {},
                            onVoteUpClick = {},
                            onFavouriteClick = {},
                            onImageClick = {},
                            onEmbedPreviewClick = {},
                            onMoreClick = {},
                            showInlineActions = false,
                        )
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    EntryCommentItem(
                        modifier = Modifier.fillMaxWidth(),
                        state = draft.comment,
                        onProfileClick = {},
                        onTagClick = {},
                        onUrlClick = {},
                        onVoteUpClick = {},
                        onFavouriteClick = {},
                        onImageClick = {},
                        onEmbedPreviewClick = {},
                        onMoreClick = {},
                        showInlineActions = false,
                    )
                }
            }
        }

        is ResourceScreenshotShareDraft.LinkComment -> {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    val parentComment = draft.parentComment
                    if (showParent && parentComment != null) {
                        LinkCommentItem(
                            modifier = Modifier.fillMaxWidth(),
                            state = parentComment,
                            onProfileClick = {},
                            onTagClick = {},
                            onUrlClick = {},
                            onVoteUpClick = {},
                            onVoteDownClick = {},
                            onFavouriteClick = {},
                            onImageClick = {},
                            onEmbedPreviewClick = {},
                            onMoreClick = {},
                            showInlineActions = false,
                        )
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    LinkCommentItem(
                        modifier = Modifier.fillMaxWidth(),
                        state = draft.comment,
                        onProfileClick = {},
                        onTagClick = {},
                        onUrlClick = {},
                        onVoteUpClick = {},
                        onVoteDownClick = {},
                        onFavouriteClick = {},
                        onImageClick = {},
                        onEmbedPreviewClick = {},
                        onMoreClick = {},
                        showInlineActions = false,
                    )
                }
            }
        }
    }
}

private suspend fun SnackbarMessage.resolveDialogSnackbarMessage(): String = when (this) {
    is SnackbarMessage.Raw -> value
    is SnackbarMessage.Resource -> getString(resource = resource, *args.toTypedArray())
}
