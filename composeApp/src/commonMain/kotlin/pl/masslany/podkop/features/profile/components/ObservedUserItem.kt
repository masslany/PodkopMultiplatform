package pl.masslany.podkop.features.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.components.Avatar
import pl.masslany.podkop.common.components.toComposeColor
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.UserItemState
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.common.preview.PodkopPreview

@Composable
fun ObservedUserItem(
    modifier: Modifier = Modifier,
    user: UserItemState,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.padding(horizontal = 16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Avatar(
                state = AvatarState(
                    type = if (user.avatarUrl.isNotBlank()) {
                        AvatarType.NetworkImage(user.avatarUrl)
                    } else {
                        AvatarType.NoAvatar
                    },
                    genderIndicatorType = user.genderIndicatorType,
                ),
                onClick = onClick,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleSmall,
                    color = user.nameColorType.toComposeColor(),
                )
                val voteReasonLabel = user.voteReason?.let { stringResource(it) }
                if (voteReasonLabel != null) {
                    Text(
                        text = voteReasonLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (user.status != "active") { // TODO
                    Text(
                        text = user.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ObservedUserItemPreview() {
    PodkopPreview(darkTheme = false) {
        ObservedUserItem(
            user = UserItemState(
                username = "alice",
                avatarUrl = "https://picsum.photos/seed/alice/96/96",
                genderIndicatorType = GenderIndicatorType.Female,
                nameColorType = NameColorType.Orange,
                online = true,
                company = false,
                verified = true,
                status = "Building things.",
            ),
            onClick = {},
        )
    }
}
