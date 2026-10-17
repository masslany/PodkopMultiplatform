package pl.masslany.podkop.common.navigation

import android.os.Build
import android.view.RoundedCorner
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The radius for a display that reports no rounded corners, as in Decompose. */
private val DefaultDisplayCornerRadius = 16.dp

@Composable
internal actual fun rememberDisplayCornerRadius(): Dp {
    val context = LocalContext.current
    val density = LocalDensity.current
    return remember(context, density) {
        val radius = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(WindowManager::class.java)
                ?.maximumWindowMetrics
                ?.windowInsets
                ?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
                ?.radius
        } else {
            null
        }
        radius?.let { with(density) { it.toDp() } } ?: DefaultDisplayCornerRadius
    }
}
