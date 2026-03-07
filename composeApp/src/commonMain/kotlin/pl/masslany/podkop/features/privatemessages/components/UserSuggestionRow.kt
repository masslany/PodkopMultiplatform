package pl.masslany.podkop.features.privatemessages.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.components.Avatar
import pl.masslany.podkop.common.components.toComposeColor
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.privatemessages.models.PrivateMessageUserSuggestionItemState
import pl.masslany.podkop.features.privatemessages.preview.PrivateMessagesPreviewFixtures

@Composable
internal fun UserSuggestionRow(
    state: PrivateMessageUserSuggestionItemState,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Avatar(
            state = state.avatarState,
            onClick = onClick,
        )
        Text(
            text = state.username,
            style = MaterialTheme.typography.bodyLarge,
            color = state.nameColorType.toComposeColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun UserSuggestionRowPreview() {
    PodkopPreview(darkTheme = false) {
        UserSuggestionRow(
            state = PrivateMessagesPreviewFixtures.newConversationState().suggestions.items.first(),
            onClick = {},
        )
    }
}
