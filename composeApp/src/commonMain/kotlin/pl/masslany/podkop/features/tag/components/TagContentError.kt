package pl.masslany.podkop.features.tag.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.tag_details_screen_error_loading_tags

@Composable
fun TagContentError() {
    Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = stringResource(resource = Res.string.tag_details_screen_error_loading_tags),
        style = MaterialTheme.typography.bodySmall,
    )
}
