package pl.masslany.podkop.features.privatemessages.newconversation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import pl.masslany.podkop.common.snackbar.LocalAppSnackbarHostState

@Composable
internal fun NewConversationScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<NewConversationViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = LocalAppSnackbarHostState.current

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { _ ->
        NewConversationScreenContent(
            state = state,
            paddingValues = paddingValues,
            onTopBarBackClicked = viewModel::onTopBarBackClicked,
            onUsernameChanged = viewModel::onUsernameChanged,
            onSuggestionClicked = viewModel::onSuggestionClicked,
            onRetrySuggestionsClicked = viewModel::onRetrySuggestionsClicked,
        )
    }
}
