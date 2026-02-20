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
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.components.Avatar
import pl.masslany.podkop.common.components.toComposeColor
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.features.profile.models.ProfileObservedUserItemState

@Composable
fun ObservedUserItem(
    user: ProfileObservedUserItemState,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
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
                if (user.status != "active") { // TODO
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
