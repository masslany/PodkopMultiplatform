package pl.masslany.podkop.common.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEvent

// The predictive Back previews of overlays that handle Back themselves: dialogs and the image viewer.

/**
 * Material's predictive Back easing. It front-loads the preview, so a short swipe already shows
 * most of the shrink, as system Back between apps does, instead of following the finger linearly.
 */
internal val PredictivePopEasing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)

/** How small an overlay gets at the end of a predictive Back preview. */
internal const val PredictivePopScale = 0.9f

/**
 * How dark the scrim over the revealed screen starts. It sets the leaving overlay apart
 * from the screen below in light mode, and black on a dark theme barely shows.
 */
internal const val PredictivePopScrimAlpha = 0.3f

/** The corner radius of an overlay that a predictive Back gesture is shrinking. */
internal val PredictivePopCornerRadius = 28.dp

/** The share of a predictive Back preview over which an overlay's corners round fully. */
internal const val PredictivePopCornerProgress = 0.15f

/**
 * Where an overlay shrinks toward: the edge opposite the swipe, so only the side the
 * gesture started from moves, and the middle when Back has no edge, such as from a button.
 */
internal fun predictivePopOrigin(swipeEdge: Int): TransformOrigin = when (swipeEdge) {
    NavigationEvent.EDGE_LEFT -> TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
    NavigationEvent.EDGE_RIGHT -> TransformOrigin(pivotFractionX = 0f, pivotFractionY = 0.5f)
    else -> TransformOrigin.Center
}
