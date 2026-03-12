package pl.masslany.podkop.features.privatemessages.newconversation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun NewConversationScreenRoot(
    paddingValues: PaddingValues,
) {
    val viewModel = koinViewModel<NewConversationViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    NewConversationScreenContent(
        state = state,
        paddingValues = paddingValues,
        onTopBarBackClicked = viewModel::onTopBarBackClicked,
        onUsernameChanged = viewModel::onUsernameChanged,
        onSuggestionClicked = viewModel::onSuggestionClicked,
        onRetrySuggestionsClicked = viewModel::onRetrySuggestionsClicked,
    )
}
