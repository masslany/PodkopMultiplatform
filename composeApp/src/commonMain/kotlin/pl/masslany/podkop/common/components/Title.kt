package pl.masslany.podkop.common.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.components.textflow.TextFlow
import pl.masslany.podkop.common.components.textflow.TextFlowObstacleAlignment
import pl.masslany.podkop.common.models.TitleState

@Composable
fun Title(
    modifier: Modifier = Modifier,
    state: TitleState,
    maxLines: Int? = null,
    textStyle: TextStyle = MaterialTheme.typography.titleSmall,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    overflow: TextOverflow = TextOverflow.Ellipsis
) {
    TextFlow(
        modifier = modifier,
        text = state.title,
        obstacleAlignment = TextFlowObstacleAlignment.TopStart,
        style = textStyle,
        color = textColor,
        maxLines = maxLines ?: state.maxLines,
        overflow = overflow,
    ) {
        if (state.isAdult && state.displayAdultBadge) {
            AdultRating(
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}
