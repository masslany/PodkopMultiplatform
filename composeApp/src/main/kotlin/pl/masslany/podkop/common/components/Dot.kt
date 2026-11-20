package pl.masslany.podkop.common.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import pl.masslany.podkop.common.preview.PodkopPreview

@Composable
fun Dot(
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    color: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
) {
    Text(
        modifier = modifier,
        text = "•",
        style = style,
        color = color,
    )
}

@Preview
@Composable
private fun DotPreview() {
    PodkopPreview(darkTheme = false) {
        Dot()
    }
}
