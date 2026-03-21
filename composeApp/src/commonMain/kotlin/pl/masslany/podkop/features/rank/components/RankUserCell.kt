package pl.masslany.podkop.features.rank.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.components.Avatar
import pl.masslany.podkop.common.components.toComposeColor
import pl.masslany.podkop.common.extensions.toMemberSinceLabel
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.rank.RankUserItemState
import pl.masslany.podkop.features.rank.preview.RankPreviewFixtures

@Composable
fun RankUserCell(
    item: RankUserItemState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RankTrendIndicator(trend = item.trend)
        Avatar(
            state = AvatarState(
                type = if (item.avatarUrl.isBlank()) {
                    AvatarType.NoAvatar
                } else {
                    AvatarType.NetworkImage(item.avatarUrl)
                },
                genderIndicatorType = item.genderIndicatorType,
            ),
            onClick = onClick,
        )
        Column(
            modifier = Modifier.weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.username,
                style = MaterialTheme.typography.titleMedium,
                color = item.nameColorType.toComposeColor(),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val memberSinceLabel = item.memberSinceState.toMemberSinceLabel()
            if (memberSinceLabel != null) {
                Text(
                    text = memberSinceLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Preview
@Composable
private fun RankUserCellPreview() {
    PodkopPreview(darkTheme = false) {
        RankUserCell(
            item = RankPreviewFixtures.leadingUser,
            onClick = {},
        )
    }
}
