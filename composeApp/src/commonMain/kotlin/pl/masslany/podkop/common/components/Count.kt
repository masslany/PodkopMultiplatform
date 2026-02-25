package pl.masslany.podkop.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import pl.masslany.podkop.common.models.CountState
import pl.masslany.podkop.common.preview.CountStateProvider
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.theme.colorsPalette
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_fire

@Composable
fun Count(
    modifier: Modifier = Modifier,
    state: CountState,
    backgroundColor: Color = Color.Transparent,
    onClick: () -> Unit,
) {
    Box(modifier = modifier) {
        BasicText(
            text = state.count,
            style = MaterialTheme.typography.labelMedium.copy(
                color = countTextColor(isHot = state.isHot, isVoted = state.isVoted),
            ),
            modifier = Modifier
                .clip(shape = RoundedCornerShape(12.dp))
                .clickable(enabled = state.canVote) { onClick() }
                .background(backgroundColorRouter(backgroundColor, state.isVoted))
                .width(42.dp)
                .height(30.dp)
                .border(
                    width = 2.dp,
                    color = borderColor(isVoted = state.isVoted),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(4.dp)
                .wrapContentSize(),
            autoSize = TextAutoSize.StepBased(
                maxFontSize = MaterialTheme.typography.labelMedium.fontSize,
            ),
            maxLines = 1,
        )
        if (state.isHot) {
            Image(
                painter = painterResource(resource = Res.drawable.ic_fire),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .offset(8.dp, 8.dp)
                    .align(Alignment.BottomEnd),
                colorFilter = ColorFilter.tint(MaterialTheme.colorsPalette.hotOrange),
            )
        }
    }
}

@Composable
private fun countTextColor(isHot: Boolean, isVoted: Boolean): Color = if (isHot) {
    if (isVoted) {
        MaterialTheme.colorsPalette.hotOrange.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorsPalette.hotOrange
    }
} else {
    if (isVoted) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
}

@Composable
private fun borderColor(isVoted: Boolean): Color = if (isVoted) {
    MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
} else {
    MaterialTheme.colorScheme.secondary
}

@Composable
private fun backgroundColorRouter(color: Color, isVoted: Boolean): Color = if (isVoted) {
    color.copy(alpha = 0.8f)
} else {
    color
}

@Preview
@Composable
private fun CountPreview(
    @PreviewParameter(CountStateProvider::class) state: CountState,
) {
    PodkopPreview(darkTheme = false) {
        Count(state = state, onClick = {})
    }
}
