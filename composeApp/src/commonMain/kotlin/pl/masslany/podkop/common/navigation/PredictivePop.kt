package pl.masslany.podkop.common.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigationevent.NavigationEvent

/** The length of a predictive Back preview, which the gesture's progress seeks through. */
private const val PredictivePopMillis = 300

/**
 * Material's predictive Back easing. It front-loads the preview, so a short swipe already shows
 * most of the shrink, as system Back between apps does, instead of following the finger linearly.
 */
internal val PredictivePopEasing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)

/** How small the leaving screen gets at the end of a predictive Back preview. */
internal const val PredictivePopScale = 0.9f

/**
 * How dark the scrim over the revealed screen starts. It sets the leaving screen apart
 * from the one below in light mode, and black on a dark theme barely shows.
 */
private const val PredictivePopScrimAlpha = 0.3f

/** The corner radius of a screen that a predictive Back gesture is shrinking. */
private val PredictivePopCornerRadius = 28.dp

/** The share of a predictive Back preview over which a leaving screen's corners round fully. */
private const val PredictivePopCornerProgress = 0.15f

/** Metadata keys of entries that `NavDisplay` shows as overlays, which keep their own shape. */
private val OverlayMetadataKeys =
    setOf(AnimatedDialogSceneStrategy.DIALOG_KEY, BottomSheetSceneStrategy.BOTTOM_SHEET_KEY)

/**
 * Where the leaving screen shrinks toward: the edge opposite the swipe, so only the side the
 * gesture started from moves, and the middle when Back has no edge, such as from a button.
 */
private fun predictivePopOrigin(swipeEdge: Int): TransformOrigin = when (swipeEdge) {
    NavigationEvent.EDGE_LEFT -> TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
    NavigationEvent.EDGE_RIGHT -> TransformOrigin(pivotFractionX = 0f, pivotFractionY = 0.5f)
    else -> TransformOrigin.Center
}

/**
 * Previews a predictive Back gesture: the leaving screen shrinks toward the edge
 * opposite the swipe and stays opaque however far the swipe goes.
 * [rememberPredictivePopCornersDecorator] rounds its corners and dims the screen below.
 */
internal fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.predictivePopTransform(
    swipeEdge: Int,
): ContentTransform = ContentTransform(
    targetContentEnter = EnterTransition.None,
    initialContentExit = scaleOut(
        animationSpec = tween(durationMillis = PredictivePopMillis, easing = PredictivePopEasing),
        targetScale = PredictivePopScale,
        transformOrigin = predictivePopOrigin(swipeEdge),
    ),
)

/**
 * How far this screen's move from [from] to [to] has played, from 0 to 1, or null otherwise.
 *
 * The duration is the scene transition's, not this screen's own: the revealed screen has no
 * animation of its own, so only its parent knows the preview's length.
 */
private fun Transition<EnterExitState>.timedProgress(from: EnterExitState, to: EnterExitState): Float? {
    val duration = parentTransition?.totalDurationNanos ?: totalDurationNanos
    if (currentState != from || targetState != to || duration <= 0L) return null
    return (playTimeNanos.toFloat() / duration).coerceIn(0f, 1f)
}

/**
 * How far this screen is through leaving by a predictive Back preview, from 0 to 1.
 *
 * Only [predictivePopTransform] takes time, so a timed scene transition is one. Reading the
 * transition's play time, rather than adding an animation to it, keeps the instant transitions
 * instant: an animation of its own would hold a popped screen on top until it ended.
 */
private fun Transition<EnterExitState>.predictivePopLeaveProgress(): Float =
    timedProgress(from = EnterExitState.Visible, to = EnterExitState.PostExit) ?: 0f

/** How far this screen is through being revealed by a predictive Back preview, or null if not. */
private fun Transition<EnterExitState>.predictivePopRevealProgress(): Float? =
    timedProgress(from = EnterExitState.PreEnter, to = EnterExitState.Visible)

private fun NavEntry<*>.isOverlay(): Boolean = metadata.keys.any { it in OverlayMetadataKeys }

/**
 * Rounds a full-screen entry's corners while a predictive Back gesture shrinks it, and dims the
 * entry it reveals until the gesture completes. Dialogs and bottom sheets are skipped:
 * `NavDisplay` hands them the scope of the screen below them.
 */
@Composable
internal fun <T : Any> rememberPredictivePopCornersDecorator(): NavEntryDecorator<T> = remember {
    NavEntryDecorator { entry ->
        if (entry.isOverlay()) {
            entry.Content()
        } else {
            val transition = LocalNavAnimatedContentScope.current.transition
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Read here, the play time redraws the layer each frame without recomposing.
                        val rounding = (transition.predictivePopLeaveProgress() / PredictivePopCornerProgress)
                            .coerceAtMost(1f)
                        if (rounding > 0f) {
                            shape = RoundedCornerShape(PredictivePopCornerRadius * rounding)
                            clip = true
                        }
                    }
                    .drawWithContent {
                        drawContent()
                        val reveal = transition.predictivePopRevealProgress() ?: return@drawWithContent
                        drawRect(Color.Black.copy(alpha = PredictivePopScrimAlpha * (1f - reveal)))
                    },
            ) {
                entry.Content()
            }
        }
    }
}
