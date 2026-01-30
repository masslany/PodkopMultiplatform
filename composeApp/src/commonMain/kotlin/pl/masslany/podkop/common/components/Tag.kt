package pl.masslany.podkop.common.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.models.TagItem
import pl.masslany.podkop.common.theme.colorsPalette
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.tag_label

@Composable
fun Tag(
    modifier: Modifier = Modifier,
    state: TagItem,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    onTagClick: (String) -> Unit,
) {
    Text(
        text = stringResource(resource = Res.string.tag_label, state.tag),
        style = textStyle,
        color = MaterialTheme.colorsPalette.tagBlue,
        modifier = modifier
            .clickable { onTagClick(state.tag) },
    )
}
