package pl.masslany.podkop.features.imageviewer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.sign
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private val DismissDragRange = 240.dp
private val DismissDistance = 120.dp
private val DismissVelocity = 900.dp
private const val DismissExitMillis = 200
private const val DismissReturnDamping = 0.85f

/**
 * The image's drag to close the viewer. [offset] and [progress] change every frame of a drag, so
 * read them where drawing happens, like `graphicsLayer`, to redraw without recomposing.
 */
@Stable
internal class ImageDismissState(private val scope: CoroutineScope, private val density: Density) {
    private val drag = Animatable(Offset.Zero, Offset.VectorConverter)

    // Ahead of drag.value, which only changes once each launched snapTo has run. Null between drags.
    private var dragTarget: Offset? = null

    val offset: Offset
        get() = drag.value

    val progress: Float
        get() = (abs(drag.value.y) / with(density) { DismissDragRange.toPx() }).coerceIn(0f, 1f)

    val isDragged: Boolean by derivedStateOf { drag.value != Offset.Zero }

    fun dragBy(delta: Offset) {
        val target = (dragTarget ?: drag.value) + delta
        dragTarget = target
        scope.launch { drag.snapTo(target) }
    }

    /**
     * Returns at once whether letting go closes the viewer, while the image animates out or back. A
     * fling closes it only away from the middle, so flicking the image back keeps it open.
     */
    fun release(
        velocity: Velocity,
        containerHeight: Float,
    ): Boolean {
        val target = dragTarget ?: drag.value
        dragTarget = null
        val closes = with(density) {
            if (abs(velocity.y) > DismissVelocity.toPx()) {
                sign(velocity.y) == sign(target.y)
            } else {
                abs(target.y) > DismissDistance.toPx()
            }
        }
        scope.launch {
            if (closes) {
                drag.animateTo(
                    targetValue = Offset(target.x, sign(target.y) * containerHeight),
                    animationSpec = tween(durationMillis = DismissExitMillis, easing = FastOutLinearInEasing),
                )
            } else {
                drag.animateTo(
                    targetValue = Offset.Zero,
                    animationSpec = spring(dampingRatio = DismissReturnDamping, stiffness = Spring.StiffnessMediumLow),
                    initialVelocity = Offset(velocity.x, velocity.y),
                )
            }
        }
        return closes
    }
}

@Composable
internal fun rememberImageDismissState(): ImageDismissState {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    return remember(scope, density) { ImageDismissState(scope = scope, density = density) }
}
