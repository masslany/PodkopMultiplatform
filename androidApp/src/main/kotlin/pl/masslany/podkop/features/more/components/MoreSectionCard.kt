package pl.masslany.podkop.features.more.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.R
import pl.masslany.podkop.common.components.SegmentedSection
import pl.masslany.podkop.common.components.segmentedRowColors
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.more.MoreActions
import pl.masslany.podkop.features.more.models.MoreSectionItemState
import pl.masslany.podkop.features.more.models.MoreSectionItemType
import pl.masslany.podkop.features.more.models.MoreSectionState
import pl.masslany.podkop.features.more.models.MoreSectionType
import pl.masslany.podkop.features.more.preview.NoOpMoreActions

@Composable
fun MoreSectionCard(
    section: MoreSectionState,
    actions: MoreActions,
    modifier: Modifier = Modifier,
) {
    if (section.items.isNotEmpty()) {
        SegmentedSection(
            modifier = modifier,
            title = stringResource(id = section.type.labelRes),
        ) {
            section.items.forEachIndexed { index, item ->
                MoreSectionRow(
                    item = item,
                    shapes = ListItemDefaults.segmentedShapes(index = index, count = section.items.size),
                    onClick = { actions.onSectionItemClicked(item.type) },
                )
            }
        }
    }
}

@Composable
private fun MoreSectionRow(
    item: MoreSectionItemState,
    shapes: ListItemShapes,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        colors = segmentedRowColors(),
        leadingContent = {
            Icon(
                imageVector = ImageVector.vectorResource(id = item.type.iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                    modifier = Modifier.rotate(180f),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        Text(
            text = stringResource(id = item.type.titleRes),
            style = MaterialTheme.typography.bodyLarge,
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
