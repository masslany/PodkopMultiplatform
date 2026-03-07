package pl.masslany.podkop.features.privatemessages.inbox

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator
import pl.masslany.podkop.common.snackbar.LocalAppSnackbarHostState

@Composable
internal fun PrivateMessagesScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<PrivateMessagesViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalAppSnackbarHostState.current
    val lazyListState = rememberLazyListPaginator(
        resetStateKey = "private_messages_inbox",
        shouldPaginate = viewModel::shouldPaginate,
        paginate = viewModel::paginate,
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { _ ->
        PrivateMessagesScreenContent(
            state = state,
            paddingValues = paddingValues,
            lazyListState = lazyListState,
            onTopBarBackClicked = viewModel::onTopBarBackClicked,
            onNewConversationClicked = viewModel::onNewConversationClicked,
            onRefresh = viewModel::onRefresh,
            onConversationClicked = viewModel::onConversationClicked,
        )
    }
}
