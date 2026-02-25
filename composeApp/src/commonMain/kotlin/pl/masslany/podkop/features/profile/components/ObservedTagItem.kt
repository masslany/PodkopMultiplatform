package pl.masslany.podkop.features.profile.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.profile.models.ProfileObservedTagItemState

@Composable
fun ObservedTagItem(
    tag: ProfileObservedTagItemState,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "#${tag.name}",
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Preview
@Composable
private fun ObservedTagItemPreview() {
    PodkopPreview(darkTheme = false) {
        ObservedTagItem(
            tag = ProfileObservedTagItemState(name = "compose", pinned = true),
            onClick = {},
        )
    }
}
