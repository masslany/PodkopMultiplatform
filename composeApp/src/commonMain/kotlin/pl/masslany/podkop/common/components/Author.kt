package pl.masslany.podkop.common.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.preview.AuthorStateProvider
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.theme.colorsPalette

@Composable
fun Author(
    state: AuthorState,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    onClick: (String) -> Unit,
) {
    Text(
        text = state.name,
        style = textStyle,
        color = state.color.toComposeColor(),
        modifier = modifier.clickable {
            onClick(state.name)
        },
    )
}

@Composable
fun NameColorType.toComposeColor(): Color = when (this) {
    NameColorType.Black -> MaterialTheme.colorsPalette.nameBlack
    NameColorType.Burgundy -> MaterialTheme.colorsPalette.nameBurgundy
    NameColorType.Green -> MaterialTheme.colorsPalette.nameGreen
    NameColorType.Orange -> MaterialTheme.colorsPalette.nameOrange
}

@Preview
@Composable
private fun AuthorPreview(
    @PreviewParameter(AuthorStateProvider::class) state: AuthorState,
) {
    PodkopPreview(darkTheme = false) {
        Author(
            state = state,
            onClick = {},
        )
    }
}
