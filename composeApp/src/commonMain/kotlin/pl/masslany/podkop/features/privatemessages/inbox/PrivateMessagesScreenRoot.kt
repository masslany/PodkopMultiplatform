package pl.masslany.podkop.features.privatemessages.inbox

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.pagination.rememberLazyListPaginator

@Composable
internal fun PrivateMessagesScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<PrivateMessagesViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListPaginator(
        resetStateKey = "private_messages_inbox",
        shouldPaginate = viewModel::shouldPaginate,
        paginate = viewModel::paginate,
    )

    PrivateMessagesNotificationPermissionEffect(
        shouldRequestPermission = state.shouldRequestNotificationPermission,
        onPermissionResult = viewModel::onNotificationPermissionResult,
    )

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
