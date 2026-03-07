package pl.masslany.podkop.features.privatemessages.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.features.privatemessages.models.ConversationScreenState

@Composable
internal fun ConversationMessagesList(
    state: ConversationScreenState,
    lazyListState: LazyListState,
    onProfileClicked: (String) -> Unit,
    onTagClicked: (String) -> Unit,
    onUrlClicked: (String) -> Unit,
    onImageClicked: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = state.messages,
            key = { message -> message.key },
        ) { message ->
            ConversationMessageBubble(
                state = message,
                onProfileClicked = onProfileClicked,
                onTagClicked = onTagClicked,
                onUrlClicked = onUrlClicked,
                onImageClicked = onImageClicked,
            )
        }
    }
}
