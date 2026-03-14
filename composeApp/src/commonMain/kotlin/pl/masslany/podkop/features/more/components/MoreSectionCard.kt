package pl.masslany.podkop.features.more.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.common.components.SectionCard
import pl.masslany.podkop.common.components.SectionCardDivider
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
        SectionCard(
            modifier = modifier,
            title = stringResource(resource = section.type.labelRes),
        ) {
            section.items.forEachIndexed { index, item ->
                MoreSectionRow(
                    item = item,
                    onClick = { actions.onSectionItemClicked(item.type) },
                )

                if (index < section.items.lastIndex) {
                    SectionCardDivider()
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
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
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
