package pl.masslany.podkop.features.resources.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.theme.colorsPalette
import pl.masslany.podkop.composeapp.R

@Composable
internal fun ResourceInlineActionsRow(
    onMoreClick: () -> Unit,
    onReplyClick: (() -> Unit)? = null,
    isReplyEnabled: Boolean = false,
    onFavouriteClick: (() -> Unit)? = null,
    isFavouriteEnabled: Boolean = false,
    isFavourite: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val disabledTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    val enabledTint = MaterialTheme.colorScheme.primary
    val defaultActionTint = MaterialTheme.colorScheme.onSurfaceVariant
    val isReplyActionEnabled = onReplyClick != null && isReplyEnabled
    val isFavouriteActionEnabled = onFavouriteClick != null && isFavouriteEnabled
    val favouriteTint = when {
        isFavourite -> MaterialTheme.colorsPalette.favouriteGold
        isFavouriteActionEnabled -> defaultActionTint
        else -> disabledTint
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .height(32.dp)
                    .clickable(
                        enabled = isReplyActionEnabled,
                        onClick = { onReplyClick?.invoke() },
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_reply),
                    contentDescription = null,
                    tint = if (isReplyActionEnabled) enabledTint else disabledTint,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.resource_actions_reply),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isReplyActionEnabled) enabledTint else disabledTint,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                enabled = isFavouriteActionEnabled,
                modifier = Modifier.size(32.dp),
                onClick = { onFavouriteClick?.invoke() },
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = ImageVector.vectorResource(
                        id = if (isFavourite) {
                            R.drawable.ic_star_filled
                        } else {
                            R.drawable.ic_star
                        },
                    ),
                    contentDescription = null,
                    tint = favouriteTint,
                )
            }
        }

        IconButton(
            modifier = Modifier
                .size(32.dp)
                .offset(12.dp),
            onClick = onMoreClick,
        ) {
            Icon(
                modifier = Modifier
                    .size(20.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_more_vert),
                contentDescription = stringResource(
                    id = R.string.accessibility_resource_more_actions,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun ResourceInlineActionsRowPreviewEnabled() {
    PodkopPreview(darkTheme = false) {
        Column {
            ResourceInlineActionsRow(
                onMoreClick = {},
                onReplyClick = {},
                isReplyEnabled = true,
                onFavouriteClick = {},
                isFavouriteEnabled = true,
                isFavourite = false,
            )
            ResourceInlineActionsRow(
                onMoreClick = {},
                onReplyClick = {},
                isReplyEnabled = true,
                onFavouriteClick = {},
                isFavouriteEnabled = true,
                isFavourite = true,
            )
        }
    }
}

@Preview
@Composable
private fun ResourceInlineActionsRowPreviewDisabled() {
    PodkopPreview(darkTheme = false) {
        ResourceInlineActionsRow(
            onMoreClick = {},
        )
    }
}
