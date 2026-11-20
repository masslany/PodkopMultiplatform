package pl.masslany.podkop.common.extensions

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
internal fun rememberWindowSizeClass(): WindowSizeClass {
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    return remember(windowInfo.containerSize, density) {
        WindowSizeClass.calculateFromSize(
            size = with(density) {
                DpSize(
                    width = windowInfo.containerSize.width.toDp(),
                    height = windowInfo.containerSize.height.toDp(),
                )
            },
        )
    }
}
