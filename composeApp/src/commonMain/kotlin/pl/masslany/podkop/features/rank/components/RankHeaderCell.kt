package pl.masslany.podkop.features.rank.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pl.masslany.podkop.common.preview.PodkopPreview

@Composable
fun RankHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        modifier = modifier,
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        textAlign = textAlign,
        maxLines = 1,
    )
}

@Preview
@Composable
private fun RankHeaderCellPreview() {
    PodkopPreview(darkTheme = false) {
        RankHeaderCell(text = "Akcje")
    }
}
