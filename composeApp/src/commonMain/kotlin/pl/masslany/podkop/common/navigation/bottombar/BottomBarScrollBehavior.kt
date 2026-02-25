package pl.masslany.podkop.common.navigation.bottombar

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

val LocalBottomBarScrollBehavior =
    staticCompositionLocalOf {
        BottomBarScrollBehavior()
    }

private const val SNAP_FLING_VELOCITY_THRESHOLD = 1000f

@Stable
class BottomBarScrollBehavior {

    var offsetPx by mutableFloatStateOf(0f)
        private set

    var heightPx by mutableFloatStateOf(0f)

    constructor()

    internal constructor(
        offsetPx: Float,
        heightPx: Float,
    ) {
        this.offsetPx = offsetPx
        this.heightPx = heightPx
    }

    fun onScroll(delta: Float) {
        offsetPx = (offsetPx + delta)
            .coerceIn(0f, heightPx)
    }

    fun reset() {
        offsetPx = 0f
        heightPx = 0f
    }

    fun snap(velocity: Float) {
        val halfway = heightPx / 2f

        offsetPx = when {
            // Strong fling → follow direction
            velocity < -SNAP_FLING_VELOCITY_THRESHOLD -> heightPx

            velocity > SNAP_FLING_VELOCITY_THRESHOLD -> 0f

            // Weak fling / drag release → use position
            offsetPx > halfway -> heightPx

            else -> 0f
        }
    }

    companion object {
        val Saver: Saver<BottomBarScrollBehavior, *> =
            listSaver(
                save = { listOf(it.offsetPx, it.heightPx) },
                restore = {
                    BottomBarScrollBehavior(
                        offsetPx = it[0],
                        heightPx = it[1],
                    )
                },
            )
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
