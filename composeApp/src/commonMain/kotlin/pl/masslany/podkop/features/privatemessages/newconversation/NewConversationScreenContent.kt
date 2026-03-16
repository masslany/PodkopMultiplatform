package pl.masslany.podkop.features.privatemessages.newconversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.common.extensions.toWindowInsets
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.privatemessages.components.UsernameSuggestionsContent
import pl.masslany.podkop.features.privatemessages.models.NewConversationScreenState
import pl.masslany.podkop.features.privatemessages.preview.PrivateMessagesPreviewFixtures
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_topbar_back
import podkop.composeapp.generated.resources.ic_arrow_back
import podkop.composeapp.generated.resources.private_messages_new_conversation_username
import podkop.composeapp.generated.resources.topbar_label_new_conversation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewConversationScreenContent(
    state: NewConversationScreenState,
    paddingValues: PaddingValues,
    onTopBarBackClicked: () -> Unit,
    onUsernameChanged: (String) -> Unit,
    onSuggestionClicked: (String) -> Unit,
    onRetrySuggestionsClicked: () -> Unit,
) {
    val topBarInsets = paddingValues.toWindowInsets(includeBottom = false)
    val contentInsets = paddingValues.toWindowInsets(includeTop = false)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(resource = Res.string.topbar_label_new_conversation))
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
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = contentInsets,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.username,
                onValueChange = onUsernameChanged,
                singleLine = true,
                label = {
                    Text(text = stringResource(resource = Res.string.private_messages_new_conversation_username))
                },
            )

            UsernameSuggestionsContent(
                state = state,
                onSuggestionClicked = onSuggestionClicked,
                onRetryClicked = onRetrySuggestionsClicked,
            )
        }
    }
}

@Preview
@Composable
private fun NewConversationScreenContentPreview() {
    PodkopPreview(darkTheme = false) {
        NewConversationScreenContent(
            state = PrivateMessagesPreviewFixtures.newConversationState(),
            paddingValues = PaddingValues(),
            onTopBarBackClicked = {},
            onUsernameChanged = {},
            onSuggestionClicked = {},
            onRetrySuggestionsClicked = {},
        )
    }
}
