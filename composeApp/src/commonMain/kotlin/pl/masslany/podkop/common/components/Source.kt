package pl.masslany.podkop.common.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Source(
    modifier: Modifier = Modifier,
    source: String,
    onSourceClick: () -> Unit,
) {
    Text(
        modifier = modifier
            .clickable(onClick = onSourceClick),
        text = source,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
    )
}
