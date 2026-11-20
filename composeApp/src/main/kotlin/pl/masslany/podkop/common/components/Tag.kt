package pl.masslany.podkop.common.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import pl.masslany.podkop.common.models.TagItem
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.preview.TagItemProvider
import pl.masslany.podkop.common.theme.colorsPalette
import pl.masslany.podkop.composeapp.R

@Composable
fun Tag(
    modifier: Modifier = Modifier,
    state: TagItem,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    onTagClick: (String) -> Unit,
) {
    Text(
        text = stringResource(id = R.string.tag_label, state.tag),
        style = textStyle,
        color = MaterialTheme.colorsPalette.tagBlue,
        modifier = modifier
            .clickable { onTagClick(state.tag) },
    )
}

@Preview
@Composable
private fun TagPreview(
    @PreviewParameter(TagItemProvider::class) state: TagItem,
) {
    PodkopPreview(darkTheme = false) {
        Tag(
            state = state,
            onTagClick = {},
        )
    }
}
