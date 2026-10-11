package pl.masslany.podkop.common.components.pagination

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.components.CommonTestTags
import pl.masslany.podkop.common.preview.PodkopPreview
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.common_retry_next_page

/**
 * Takes the loading row's place when the next page failed to load. Scrolling does not retry a
 * failed page, so the list would stop there; as on iOS, this asks for it again.
 */
@Composable
fun PaginationErrorItem(
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        OutlinedButton(
            modifier = Modifier.testTag(CommonTestTags.Pagination.Retry),
            onClick = onRetryClick,
        ) {
            Text(text = stringResource(resource = Res.string.common_retry_next_page))
        }
    }
}

@Preview
@Composable
private fun PaginationErrorItemPreview() {
    PodkopPreview(darkTheme = false) {
        PaginationErrorItem(onRetryClick = {})
    }
}
