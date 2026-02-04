package pl.masslany.podkop.common.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import pl.masslany.podkop.common.models.DescriptionState

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
