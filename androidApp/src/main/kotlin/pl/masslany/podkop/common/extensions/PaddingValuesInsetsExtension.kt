package pl.masslany.podkop.common.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@Composable
internal fun PaddingValues.toWindowInsets(
    includeTop: Boolean = true,
    includeBottom: Boolean = true,
): WindowInsets {
    val layoutDirection = LocalLayoutDirection.current
    return WindowInsets(
        left = calculateStartPadding(layoutDirection),
        top = if (includeTop) calculateTopPadding() else 0.dp,
        right = calculateEndPadding(layoutDirection),
        bottom = if (includeBottom) calculateBottomPadding() else 0.dp,
    )
}
