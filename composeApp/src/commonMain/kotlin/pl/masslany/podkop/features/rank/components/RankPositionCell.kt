package pl.masslany.podkop.features.rank.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.theme.colorsPalette

@Composable
fun RankPositionCell(
    position: Int,
    modifier: Modifier = Modifier,
) {
    val isTopThree = position in 1..3
    Text(
        modifier = modifier,
        text = position.toString(),
        style = MaterialTheme.typography.headlineSmall,
        color = if (isTopThree) {
            MaterialTheme.colorsPalette.nameOrange
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        fontWeight = if (isTopThree) FontWeight.Bold else FontWeight.Medium,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

@Preview
@Composable
private fun RankPositionCellPreview() {
    PodkopPreview(darkTheme = false) {
        RankPositionCell(position = 1)
    }
}
