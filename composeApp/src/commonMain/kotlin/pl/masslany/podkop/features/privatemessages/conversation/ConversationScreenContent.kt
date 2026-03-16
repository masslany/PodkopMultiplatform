package pl.masslany.podkop.features.privatemessages.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.common.components.GenericErrorScreen
import pl.masslany.podkop.common.composer.Composer
import pl.masslany.podkop.common.extensions.rememberWindowSizeClass
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.privatemessages.components.ConversationMessagesList
import pl.masslany.podkop.features.privatemessages.models.ConversationScreenState
import pl.masslany.podkop.features.privatemessages.preview.NoOpConversationActions
import pl.masslany.podkop.features.privatemessages.preview.PrivateMessagesPreviewFixtures
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_private_messages_collapse_composer
import podkop.composeapp.generated.resources.accessibility_private_messages_expand_composer
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.composer_button_send
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.ic_keyboard_arrow_up
import podkop.composeapp.generated.resources.private_messages_composer_hint
import podkop.composeapp.generated.resources.private_messages_thread_empty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConversationScreenContent(
    state: ConversationScreenState,
    paddingValues: PaddingValues,
    lazyListState: LazyListState,
    actions: ConversationActions,
) {
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)
    val contentInsets = paddingValues.toWindowInsets(includeTop = false)
    val windowSizeClass = rememberWindowSizeClass()
    val canCollapseComposer =
        windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact &&
            windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    var isComposerCollapsed by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val composerHint = stringResource(resource = Res.string.private_messages_composer_hint)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.username,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = actions::onTopBarBackClicked) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(resource = Res.string.accessibility_topbar_back),
                        )
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                windowInsets = topBarInsets,
            )
        },
        bottomBar = {
            if (canCollapseComposer && isComposerCollapsed) {
                CollapsedConversationComposerBar(
                    text = state.composer.content.text.ifBlank { composerHint },
                    contentDescription = stringResource(
                        resource = Res.string.accessibility_private_messages_expand_composer,
                    ),
                    modifier = Modifier.padding(contentInsets.asPaddingValues()),
                    onExpand = {
                        isComposerCollapsed = false
                    },
                )
            } else {
                Composer(
                    state = state.composer,
                    hintText = composerHint,
                    submitText = stringResource(resource = Res.string.composer_button_send),
                    autoFocus = false,
                    submitEnabled = state.canSubmit,
                    showDismissButton = false,
                    actionsTrailingContent = if (canCollapseComposer) {
                        {
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus(force = true)
                                    keyboardController?.hide()
                                    isComposerCollapsed = true
                                },
                            ) {
                                Icon(
                                    modifier = Modifier.graphicsLayer {
                                        rotationZ = 180f
                                    },
                                    imageVector = vectorResource(resource = Res.drawable.ic_keyboard_arrow_up),
                                    contentDescription = stringResource(
                                        resource = Res.string.accessibility_private_messages_collapse_composer,
                                    ),
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onContentChanged = actions::onComposerTextChanged,
                    onAdultChanged = actions::onComposerAdultChanged,
                    onPhotoAttachClicked = actions::onComposerPhotoAttachClicked,
                    onPhotoRemoved = actions::onComposerPhotoRemoved,
                    onDismiss = actions::onTopBarBackClicked,
                    onSubmit = actions::onComposerSubmit,
                    modifier = Modifier.padding(contentInsets.asPaddingValues()),
                    applyBottomInsetsPadding = false,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = contentInsets,
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = actions::onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }

                state.isError -> {
                    GenericErrorScreen(
                        modifier = Modifier
                            .padding(top = 32.dp)
                            .fillMaxSize()
                            .align(Alignment.Center),
                        onRefreshClicked = actions::onRetryClicked,
                    )
                }

                state.messages.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 24.dp),
                            text = stringResource(resource = Res.string.private_messages_thread_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> {
                    ConversationMessagesList(
                        state = state,
                        lazyListState = lazyListState,
                        onProfileClicked = actions::onProfileClicked,
                        onTagClicked = actions::onTagClicked,
                        onUrlClicked = actions::onUrlClicked,
                        onImageClicked = actions::onImageClicked,
                    )
                }
            }
        }
    }
}

@Composable
private fun CollapsedConversationComposerBar(
    text: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onExpand: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onExpand) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = vectorResource(resource = Res.drawable.ic_keyboard_arrow_up),
                        contentDescription = contentDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ConversationScreenContentPreview() {
    PodkopPreview(darkTheme = false) {
        ConversationScreenContent(
            state = PrivateMessagesPreviewFixtures.conversationState(),
            paddingValues = PaddingValues(),
            lazyListState = rememberLazyListState(),
            actions = NoOpConversationActions,
        )
    }
}

@Preview(widthDp = 640, heightDp = 360)
@Composable
private fun ConversationScreenContentLandscapePreview() {
    PodkopPreview(darkTheme = false) {
        ConversationScreenContent(
            state = PrivateMessagesPreviewFixtures.conversationState(),
            paddingValues = PaddingValues(),
            lazyListState = rememberLazyListState(),
            actions = NoOpConversationActions,
        )
    }
}
