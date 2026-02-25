package pl.masslany.podkop.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.preview.PodkopPreview
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.generic_error_body
import podkop.composeapp.generated.resources.generic_error_title
import podkop.composeapp.generated.resources.refresh_button

@Composable
fun GenericErrorScreen(
    onRefreshClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(resource = Res.string.generic_error_title),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(resource = Res.string.generic_error_body),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRefreshClicked) {
            Text(text = stringResource(resource = Res.string.refresh_button))
        }
    }
}

@Preview
@Composable
private fun GenericErrorScreenPreview() {
    PodkopPreview(darkTheme = false) {
        GenericErrorScreen(
            modifier = Modifier.padding(16.dp),
            onRefreshClicked = {},
        )
    }
}
