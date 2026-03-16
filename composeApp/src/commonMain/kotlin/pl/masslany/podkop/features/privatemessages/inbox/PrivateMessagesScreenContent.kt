package pl.masslany.podkop.features.privatemessages.inbox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.common.components.GenericErrorScreen
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.privatemessages.components.PrivateMessagesInboxList
import pl.masslany.podkop.features.privatemessages.models.PrivateMessagesScreenState
import pl.masslany.podkop.features.privatemessages.preview.PrivateMessagesPreviewFixtures
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_private_messages_new_conversation
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.ic_add
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.private_messages_empty
import podkop.composeapp.generated.resources.topbar_label_private_messages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrivateMessagesScreenContent(
    state: PrivateMessagesScreenState,
    paddingValues: PaddingValues,
    lazyListState: LazyListState,
    onTopBarBackClicked: () -> Unit,
    onNewConversationClicked: () -> Unit,
    onRefresh: () -> Unit,
    onConversationClicked: (String) -> Unit,
) {
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)
    val contentInsets = paddingValues.toWindowInsets(includeTop = false)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.topbar_label_private_messages))
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
                windowInsets = topBarInsets,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(16.dp),
                onClick = onNewConversationClicked,
            ) {
                Icon(
                    imageVector = vectorResource(resource = Res.drawable.ic_add),
                    contentDescription = stringResource(
                        resource = Res.string.accessibility_private_messages_new_conversation,
                    ),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = contentInsets,
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
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
                        onRefreshClicked = onRefresh,
                    )
                }

                state.conversations.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 24.dp),
                            text = stringResource(resource = Res.string.private_messages_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                else -> {
                    PrivateMessagesInboxList(
                        state = state,
                        lazyListState = lazyListState,
                        bottomPadding = 0.dp,
                        onConversationClicked = onConversationClicked,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PrivateMessagesScreenContentPreview() {
    PodkopPreview(darkTheme = false) {
        PrivateMessagesScreenContent(
            state = PrivateMessagesPreviewFixtures.inboxState(),
            paddingValues = PaddingValues(),
            lazyListState = rememberLazyListState(),
            onTopBarBackClicked = {},
            onNewConversationClicked = {},
            onRefresh = {},
            onConversationClicked = {},
        )
    }
}
