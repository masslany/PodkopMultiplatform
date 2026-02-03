package pl.masslany.podkop.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.common.theme.colorsPalette

@Composable
fun GenderIndicator(
    modifier: Modifier = Modifier,
    type: GenderIndicatorType,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(type.toComposeColor())
    )
}

@Composable
fun GenderIndicatorType.toComposeColor(): Color {
    return when (this) {
        GenderIndicatorType.Female -> MaterialTheme.colorsPalette.genderPink
        GenderIndicatorType.Male -> MaterialTheme.colorsPalette.genderBlue
        GenderIndicatorType.Unspecified -> MaterialTheme.colorsPalette.genderGray
    }
}
