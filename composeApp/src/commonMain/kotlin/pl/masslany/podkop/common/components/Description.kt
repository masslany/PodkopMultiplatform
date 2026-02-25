package pl.masslany.podkop.common.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import pl.masslany.podkop.common.models.DescriptionState
import pl.masslany.podkop.common.preview.DescriptionStateProvider
import pl.masslany.podkop.common.preview.PodkopPreview

@Composable
fun Description(
    modifier: Modifier = Modifier,
    state: DescriptionState,
) {
    Text(
        text = state.description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = state.maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun DescriptionPreview(
    @PreviewParameter(DescriptionStateProvider::class) state: DescriptionState,
) {
    PodkopPreview(darkTheme = false) {
        Description(state = state)
    }
}
