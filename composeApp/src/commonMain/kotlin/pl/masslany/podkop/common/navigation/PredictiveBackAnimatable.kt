/*
 * Adapted from MaterialPredictiveBackAnimatable in Decompose 3.6.0-alpha01, Copyright Arkadii Ivanov,
 * licensed under the Apache License, Version 2.0: https://github.com/arkivanov/Decompose
 */

package pl.masslany.podkop.common.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEvent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/** How small the leaving screen gets at the end of a predictive Back gesture. */
private const val PredictiveBackExitScale = 0.9f

/** How far the leaving screen keeps from the edges: sideways by the end, and up or down as it follows the touch. */
private val PredictiveBackEdgeMargin = 8.dp

/** How dark the scrim over the revealed screen is. */
private const val PredictiveBackScrimAlpha = 0.25f

/**
 * Animates the two screens of a predictive Back gesture, as X does with Decompose. The leaving
 * screen shrinks toward the edge opposite the swipe and follows the touch up and down a little,
 * and the revealed one waits under a scrim. A completed gesture fades the leaving screen out, and
 * a cancelled one springs it back.
 */
@Stable
internal class PredictiveBackAnimatable(initialEvent: NavigationEvent) {

    private val progressAnimatable = Animatable(initialValue = initialEvent.progress)
    private val finishProgressAnimatable = Animatable(initialValue = 1f)
    private val initialTouchY = initialEvent.touchY
    private var swipeEdge by mutableIntStateOf(initialEvent.swipeEdge)
    private var touchY by mutableFloatStateOf(initialEvent.touchY)

    fun exitModifier(cornerRadius: Dp): Modifier = Modifier.graphicsLayer {
        val progress = progressAnimatable.value
        val scale = 1f - progress * (1f - PredictiveBackExitScale)
        scaleX = scale
        scaleY = scale
        alpha = finishProgressAnimatable.value
        translationX = exitOffsetX(scale = scale, progress = progress)
        translationY = exitOffsetY(progress = progress)
        shape = RoundedCornerShape(cornerRadius)
        clip = true
    }

    fun enterModifier(): Modifier = Modifier.drawWithContent {
        drawContent()
        drawRect(color = Color.Black.copy(alpha = finishProgressAnimatable.value * PredictiveBackScrimAlpha))
    }

    private fun GraphicsLayerScope.exitOffsetX(scale: Float, progress: Float): Float {
        val margin = PredictiveBackEdgeMargin.toPx() * progress
        return when (swipeEdge) {
            NavigationEvent.EDGE_LEFT -> (size.width - size.width * scale) / 2f - margin
            NavigationEvent.EDGE_RIGHT -> (size.width * scale - size.width) / 2f + margin
            else -> 0f
        }
    }

    private fun GraphicsLayerScope.exitOffsetY(progress: Float): Float {
        if (size.height == 0f) return 0f
        val limit = size.height / 20f - PredictiveBackEdgeMargin.toPx()
        val factor = (touchY - initialTouchY) / size.height * (progress * 3f).coerceAtMost(1f)
        return limit * factor
    }

    suspend fun animate(event: NavigationEvent) {
        swipeEdge = event.swipeEdge
        touchY = event.touchY
        progressAnimatable.animateTo(targetValue = event.progress)
    }

    suspend fun finish() {
        val velocityFactor = progressAnimatable.velocity.coerceAtMost(1f)
        val progress = progressAnimatable.value
        coroutineScope {
            launch { progressAnimatable.animateTo(targetValue = progress + (1f - progress) * velocityFactor) }
            launch { finishProgressAnimatable.animateTo(targetValue = 0f) }
        }
    }

    suspend fun cancel() {
        progressAnimatable.animateTo(targetValue = 0f)
    }
}
