package pl.masslany.podkop.features.rank.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.rank.RankCompactActionsColumnWidth
import pl.masslany.podkop.features.rank.RankMetricColumnWidth
import pl.masslany.podkop.features.rank.RankPositionColumnWidth
import pl.masslany.podkop.features.rank.RankUserItemState
import pl.masslany.podkop.features.rank.preview.RankPreviewFixtures

@Composable
fun RankRow(
    item: RankUserItemState,
    showExtendedColumns: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RankPositionCell(
                modifier = Modifier.width(RankPositionColumnWidth),
                position = item.position,
            )
            RankUserCell(
                modifier = Modifier.weight(1f),
                item = item,
                onClick = onClick,
            )
            if (showExtendedColumns) {
                RankMetricCell(
                    modifier = Modifier.width(RankMetricColumnWidth),
                    value = item.linksCount,
                )
                RankMetricCell(
                    modifier = Modifier.width(RankMetricColumnWidth),
                    value = item.entriesCount,
                )
                RankMetricCell(
                    modifier = Modifier.width(RankMetricColumnWidth),
                    value = item.followersCount,
                )
            }
            RankMetricCell(
                modifier = Modifier.width(
                    if (showExtendedColumns) {
                        RankMetricColumnWidth
                    } else {
                        RankCompactActionsColumnWidth
                    },
                ),
                value = item.actionsCount,
            )
        }
    }
}

@Preview(name = "Compact", widthDp = 390)
@Composable
private fun RankRowCompactPreview() {
    PodkopPreview(darkTheme = false) {
        RankRow(
            item = RankPreviewFixtures.fallingUser,
            showExtendedColumns = false,
            onClick = {},
        )
    }
}

@Preview(name = "Wide", widthDp = 960)
@Composable
private fun RankRowWidePreview() {
    PodkopPreview(darkTheme = false) {
        RankRow(
            item = RankPreviewFixtures.risingUser,
            showExtendedColumns = true,
            onClick = {},
        )
    }
}
