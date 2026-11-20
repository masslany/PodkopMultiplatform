package pl.masslany.podkop.features.tag.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.composeapp.R

@Composable
fun TagContentError() {
    Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = stringResource(id = R.string.tag_details_screen_error_loading_tags),
        style = MaterialTheme.typography.bodySmall,
    )
}
