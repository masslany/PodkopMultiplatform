package pl.masslany.podkop.common.navigation.bottombar

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

val LocalBottomBarScrollBehavior =
    staticCompositionLocalOf<BottomBarScrollBehavior> {
        error("No BottomBarScrollBehavior provided")
    }

@Stable
class BottomBarScrollBehavior {

    var offsetPx by mutableFloatStateOf(0f)
        private set

    var heightPx by mutableFloatStateOf(0f)

    fun onScroll(delta: Float) {
        offsetPx = (offsetPx + delta)
            .coerceIn(0f, heightPx)
    }

    fun snap(velocity: Float) {
        offsetPx = if (velocity < 0) heightPx else 0f
    }
}

fun BottomBarScrollBehavior.nestedScrollConnection() =
    object : NestedScrollConnection {

        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource,
        ): Offset {
            val dy = available.y

            onScroll(-dy)

            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            snap(available.y)
            return Velocity.Zero
        }
    }
