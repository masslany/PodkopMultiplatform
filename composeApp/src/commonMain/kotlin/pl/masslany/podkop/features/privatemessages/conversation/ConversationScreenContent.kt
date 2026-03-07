package pl.masslany.podkop.features.privatemessages.conversation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.common.components.GenericErrorScreen
import pl.masslany.podkop.common.composer.Composer
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.privatemessages.components.ConversationMessagesList
import pl.masslany.podkop.features.privatemessages.models.ConversationScreenState
import pl.masslany.podkop.features.privatemessages.preview.PrivateMessagesPreviewFixtures
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.composer_button_send
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.private_messages_composer_hint
import podkop.composeapp.generated.resources.private_messages_thread_empty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConversationScreenContent(
    state: ConversationScreenState,
    paddingValues: PaddingValues,
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
    onTopBarBackClicked: () -> Unit,
    onRefresh: () -> Unit,
    onRetryClicked: () -> Unit,
    onComposerTextChanged: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    onComposerAdultChanged: (Boolean) -> Unit,
    onComposerPhotoAttachClicked: () -> Unit,
    onComposerPhotoRemoved: () -> Unit,
    onComposerSubmit: () -> Unit,
    onProfileClicked: (String) -> Unit,
    onTagClicked: (String) -> Unit,
    onUrlClicked: (String) -> Unit,
    onImageClicked: (String) -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = paddingValues.calculateStartPadding(LocalLayoutDirection.current),
                end = paddingValues.calculateEndPadding(LocalLayoutDirection.current),
            ),
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
                    IconButton(onClick = onTopBarBackClicked) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(resource = Res.string.accessibility_topbar_back),
                        )
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                windowInsets = WindowInsets(top = paddingValues.calculateTopPadding()),
            )
        },
        bottomBar = {
            Composer(
                state = state.composer,
                hintText = stringResource(resource = Res.string.private_messages_composer_hint),
                submitText = stringResource(resource = Res.string.composer_button_send),
                autoFocus = false,
                submitEnabled = state.canSubmit,
                showDismissButton = false,
                onContentChanged = onComposerTextChanged,
                onAdultChanged = onComposerAdultChanged,
                onPhotoAttachClicked = onComposerPhotoAttachClicked,
                onPhotoRemoved = onComposerPhotoRemoved,
                onDismiss = onTopBarBackClicked,
                onSubmit = onComposerSubmit,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding()),
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
                        onRefreshClicked = onRetryClicked,
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
                        onProfileClicked = onProfileClicked,
                        onTagClicked = onTagClicked,
                        onUrlClicked = onUrlClicked,
                        onImageClicked = onImageClicked,
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
            lazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
            onTopBarBackClicked = {},
            onRefresh = {},
            onRetryClicked = {},
            onComposerTextChanged = {},
            onComposerAdultChanged = {},
            onComposerPhotoAttachClicked = {},
            onComposerPhotoRemoved = {},
            onComposerSubmit = {},
            onProfileClicked = {},
            onTagClicked = {},
            onUrlClicked = {},
            onImageClicked = {},
        )
    }
}
