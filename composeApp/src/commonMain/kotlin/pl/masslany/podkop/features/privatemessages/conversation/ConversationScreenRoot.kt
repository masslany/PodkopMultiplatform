package pl.masslany.podkop.features.privatemessages.conversation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pl.masslany.podkop.features.privatemessages.ConversationScreen
import pl.masslany.podkop.features.privatemessages.models.ConversationPaginationState

@Composable
internal fun ConversationScreenRoot(
    screen: ConversationScreen,
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<ConversationViewModel>(
        parameters = { parametersOf(screen) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()

    LifecycleStartEffect(viewModel) {
        viewModel.onScreenStarted()
        onStopOrDispose {
            viewModel.onScreenStopped()
        }
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            ConversationPaginationState(
                firstVisibleItemIndex = lazyListState.firstVisibleItemIndex,
                canScrollForward = lazyListState.canScrollForward,
            )
        }.collect { paginationState ->
            if (
                viewModel.shouldPaginate(
                    firstVisibleItemIndex = paginationState.firstVisibleItemIndex,
                    canScrollForward = paginationState.canScrollForward,
                )
            ) {
                viewModel.paginate()
            }
        }
    }

    LaunchedEffect(state.scrollToLatestMessage) {
        if (state.scrollToLatestMessage > 0 && state.messages.isNotEmpty()) {
            lazyListState.scrollToItem(index = state.messages.lastIndex)
        }
    }

    ConversationScreenContent(
        state = state,
        paddingValues = paddingValues,
        lazyListState = lazyListState,
        onTopBarBackClicked = viewModel::onTopBarBackClicked,
        onRefresh = viewModel::onRefresh,
        onRetryClicked = viewModel::onRetryClicked,
        onComposerTextChanged = viewModel::onComposerTextChanged,
        onComposerAdultChanged = viewModel::onComposerAdultChanged,
        onComposerPhotoAttachClicked = viewModel::onComposerPhotoAttachClicked,
        onComposerPhotoRemoved = viewModel::onComposerPhotoRemoved,
        onComposerSubmit = viewModel::onComposerSubmit,
        onProfileClicked = viewModel::onProfileClicked,
        onTagClicked = viewModel::onTagClicked,
        onUrlClicked = viewModel::onUrlClicked,
        onImageClicked = viewModel::onImageClicked,
    )
}
