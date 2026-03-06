package pl.masslany.podkop.features.more.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.more.MoreActions
import pl.masslany.podkop.features.more.models.MoreSectionItemState
import pl.masslany.podkop.features.more.models.MoreSectionItemType
import pl.masslany.podkop.features.more.models.MoreSectionState
import pl.masslany.podkop.features.more.models.MoreSectionType
import pl.masslany.podkop.features.more.preview.NoOpMoreActions
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_arrow_back

@Composable
fun MoreSectionCard(
    section: MoreSectionState,
    actions: MoreActions,
    modifier: Modifier = Modifier,
) {
    if (section.items.isNotEmpty()) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(resource = section.type.labelRes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                ),
            ) {
                Column {
                    section.items.forEachIndexed { index, item ->
                        MoreSectionRow(
                            item = item,
                            onClick = { actions.onSectionItemClicked(item.type) },
                        )

                        if (index < section.items.lastIndex) {
                            HorizontalDivider(
                                thickness = Dp.Hairline,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.padding(start = 52.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreSectionRow(
    item: MoreSectionItemState,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = vectorResource(resource = item.type.iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(resource = item.type.titleRes),
            style = MaterialTheme.typography.bodyLarge,
        )

        if (item.badgeCount != null && item.badgeCount > 0) {
            Badge(
                containerColor = MaterialTheme.colorScheme.error,
            ) {
                Text(
                    text = item.badgeCount.toString(),
                    color = MaterialTheme.colorScheme.onError,
                )
            }
        }

        Icon(
            modifier = Modifier
                .size(20.dp)
                .rotate(180f),
            imageVector = vectorResource(resource = Res.drawable.ic_arrow_back),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun MoreSectionCardPreview() {
    PodkopPreview(darkTheme = false) {
        MoreSectionCard(
            section = MoreSectionState(
                type = MoreSectionType.Social,
                items = persistentListOf(
                    MoreSectionItemState(
                        type = MoreSectionItemType.Notifications,
                        badgeCount = 5,
                    ),
                    MoreSectionItemState(
                        type = MoreSectionItemType.Messages,
                        badgeCount = 0,
                    ),
                ),
            ),
            actions = NoOpMoreActions,
        )
    }
}
