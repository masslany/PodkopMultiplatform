package pl.masslany.podkop.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.preview.PodkopPreview
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.comment_button_show_blacklisted_content
import podkop.composeapp.generated.resources.comment_label_blacklisted_user

@Composable
fun BlacklistedContentGate(
    isBlacklisted: Boolean,
    revealKey: Any,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var revealed by remember(revealKey) { mutableStateOf(false) }

    if (!isBlacklisted || revealed) {
        content()
    } else {
        Column(modifier = modifier) {
            Text(
                text = stringResource(resource = Res.string.comment_label_blacklisted_user),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                modifier = Modifier.height(24.dp),
                onClick = { revealed = true },
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp),
            ) {
                Text(
                    text = stringResource(resource = Res.string.comment_button_show_blacklisted_content),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Preview
@Composable
private fun BlacklistedContentGatePreview() {
    PodkopPreview(darkTheme = false) {
        BlacklistedContentGate(
            isBlacklisted = true,
            revealKey = "",
        ) {
            Text(text = "Content")
        }
    }
}
