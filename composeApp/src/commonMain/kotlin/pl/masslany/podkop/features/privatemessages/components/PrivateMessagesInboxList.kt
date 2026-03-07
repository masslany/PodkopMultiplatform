package pl.masslany.podkop.features.privatemessages.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.features.privatemessages.models.PrivateMessagesScreenState

@Composable
internal fun PrivateMessagesInboxList(
    state: PrivateMessagesScreenState,
    lazyListState: LazyListState,
    bottomPadding: Dp,
    onConversationClicked: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(
            top = 12.dp,
            bottom = bottomPadding + 96.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = state.conversations,
            key = { conversation -> conversation.username },
        ) { conversation ->
            InboxConversationCard(
                state = conversation,
                onClick = { onConversationClicked(conversation.username) },
            )
        }
    }
}
