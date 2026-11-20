package pl.masslany.podkop.features.rank.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pl.masslany.podkop.common.preview.PodkopPreview

@Composable
fun RankMetricCell(
    value: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = value.toString(),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.End,
        maxLines = 1,
    )
}

@Preview
@Composable
private fun RankMetricCellPreview() {
    PodkopPreview(darkTheme = false) {
        RankMetricCell(value = 175794)
    }
}
