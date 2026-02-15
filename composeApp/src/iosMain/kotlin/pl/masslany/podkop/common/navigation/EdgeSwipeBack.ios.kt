package pl.masslany.podkop.common.navigation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import kotlin.math.abs

actual fun Modifier.edgeSwipeBackGesture(
    enabled: Boolean,
    navigationInput: DirectNavigationEventInput?,
): Modifier {
    if (!enabled) return this

    return pointerInput(enabled) {
        val edgeWidthPx = 24.dp.toPx()
        val completionThreshold = 0.35f

        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            if (down.position.x > edgeWidthPx) return@awaitEachGesture

            var totalDx = 0f
            var totalDy = 0f
            var started = false
            var currentProgress = 0f
            val pointerId = down.id

            do {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == pointerId } ?: continue
                if (!change.pressed) break

                val delta = change.position - change.previousPosition
                totalDx += delta.x
                totalDy += delta.y
                val mostlyHorizontal = abs(totalDx) > abs(totalDy) * 1.15f

                if (!started && totalDx > 0f && mostlyHorizontal) {
                    started = true
                    navigationInput?.backStarted(
                        event = NavigationEvent(
                            swipeEdge = NavigationEvent.EDGE_LEFT,
                            progress = 0f,
                            touchX = change.position.x,
                            touchY = change.position.y,
                        ),
                    )
                }

                if (started) {
                    currentProgress = (totalDx / size.width.toFloat()).coerceIn(0f, 1f)
                    navigationInput?.backProgressed(
                        event = NavigationEvent(
                            swipeEdge = NavigationEvent.EDGE_LEFT,
                            progress = currentProgress,
                            touchX = change.position.x,
                            touchY = change.position.y,
                        ),
                    )
                }
            } while (event.changes.any { it.pressed })

            if (started) {
                if (currentProgress >= completionThreshold) {
                    navigationInput?.backCompleted()
                } else {
                    navigationInput?.backCancelled()
                }
            }
        }
    }
}
