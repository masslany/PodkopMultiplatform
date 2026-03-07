package pl.masslany.podkop.features.privatemessages.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.privatemessages.models.NewConversationScreenState
import pl.masslany.podkop.features.privatemessages.models.UserSuggestionsState
import pl.masslany.podkop.features.privatemessages.models.UserSuggestionsStatus
import pl.masslany.podkop.features.privatemessages.preview.PrivateMessagesPreviewFixtures
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.private_messages_new_conversation_helper
import podkop.composeapp.generated.resources.private_messages_new_conversation_suggestions_error
import podkop.composeapp.generated.resources.private_messages_user_suggestions_min_query
import podkop.composeapp.generated.resources.refresh_button
import podkop.composeapp.generated.resources.search_screen_no_results

internal const val MinUsernameQueryLength = 3

@Composable
internal fun UsernameSuggestionsContent(
    state: NewConversationScreenState,
    onSuggestionClicked: (String) -> Unit,
    onRetryClicked: () -> Unit,
) {
    val normalizedUsername = state.normalizedUsername

    when (state.suggestions.status) {
        UserSuggestionsStatus.Hidden -> {
            val helperText = if (normalizedUsername.isBlank() || normalizedUsername.length >= MinUsernameQueryLength) {
                stringResource(resource = Res.string.private_messages_new_conversation_helper)
            } else {
                stringResource(
                    resource = Res.string.private_messages_user_suggestions_min_query,
                    MinUsernameQueryLength,
                )
            }
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        UserSuggestionsStatus.Loading -> {
            SuggestionCard {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        }

        UserSuggestionsStatus.Error -> {
            SuggestionCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(resource = Res.string.private_messages_new_conversation_suggestions_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = onRetryClicked) {
                        Text(text = stringResource(resource = Res.string.refresh_button))
                    }
                }
            }
        }

        UserSuggestionsStatus.Content -> {
            SuggestionCard {
                if (state.suggestions.items.isEmpty()) {
                    Text(
                        text = stringResource(resource = Res.string.search_screen_no_results),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        state.suggestions.items.forEachIndexed { index, item ->
                            UserSuggestionRow(
                                state = item,
                                onClick = { onSuggestionClicked(item.username) },
                            )
                            if (index != state.suggestions.items.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun UsernameSuggestionsContentPreview() {
    PodkopPreview(darkTheme = false) {
        UsernameSuggestionsContent(
            state = PrivateMessagesPreviewFixtures.newConversationState(),
            onSuggestionClicked = {},
            onRetryClicked = {},
        )
    }
}

@Preview
@Composable
private fun UsernameSuggestionsContentErrorPreview() {
    PodkopPreview(darkTheme = false) {
        UsernameSuggestionsContent(
            state = PrivateMessagesPreviewFixtures.newConversationState().copy(
                suggestions = UserSuggestionsState.initial.copy(
                    status = UserSuggestionsStatus.Error,
                ),
            ),
            onSuggestionClicked = {},
            onRetryClicked = {},
        )
    }
}
