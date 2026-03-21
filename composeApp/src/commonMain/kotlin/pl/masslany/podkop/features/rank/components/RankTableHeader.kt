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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.rank.RankCompactActionsColumnWidth
import pl.masslany.podkop.features.rank.RankMetricColumnWidth
import pl.masslany.podkop.features.rank.RankPositionColumnWidth
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.profile_summary_actions
import podkop.composeapp.generated.resources.profile_summary_entries
import podkop.composeapp.generated.resources.profile_summary_followers
import podkop.composeapp.generated.resources.profile_summary_links
import podkop.composeapp.generated.resources.rank_header_position
import podkop.composeapp.generated.resources.rank_header_user

@Composable
fun RankTableHeader(
    showExtendedColumns: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RankHeaderCell(
                modifier = Modifier.width(RankPositionColumnWidth),
                text = stringResource(resource = Res.string.rank_header_position),
                textAlign = TextAlign.Center,
            )
            RankHeaderCell(
                modifier = Modifier.weight(1f),
                text = stringResource(resource = Res.string.rank_header_user),
            )
            if (showExtendedColumns) {
                RankHeaderCell(
                    modifier = Modifier.width(RankMetricColumnWidth),
                    text = stringResource(resource = Res.string.profile_summary_links),
                    textAlign = TextAlign.End,
                )
                RankHeaderCell(
                    modifier = Modifier.width(RankMetricColumnWidth),
                    text = stringResource(resource = Res.string.profile_summary_entries),
                    textAlign = TextAlign.End,
                )
                RankHeaderCell(
                    modifier = Modifier.width(RankMetricColumnWidth),
                    text = stringResource(resource = Res.string.profile_summary_followers),
                    textAlign = TextAlign.End,
                )
            }
            RankHeaderCell(
                modifier = Modifier.width(
                    if (showExtendedColumns) {
                        RankMetricColumnWidth
                    } else {
                        RankCompactActionsColumnWidth
                    },
                ),
                text = stringResource(resource = Res.string.profile_summary_actions),
                textAlign = TextAlign.End,
            )
        }
    }
}

@Preview(name = "Compact", widthDp = 390)
@Composable
private fun RankTableHeaderCompactPreview() {
    PodkopPreview(darkTheme = false) {
        RankTableHeader(showExtendedColumns = false)
    }
}

@Preview(name = "Wide", widthDp = 960)
@Composable
private fun RankTableHeaderWidePreview() {
    PodkopPreview(darkTheme = false) {
        RankTableHeader(showExtendedColumns = true)
    }
}
