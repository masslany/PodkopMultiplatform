package pl.masslany.podkop.common.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.stringResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.image_actions_label

val LocalImageActions = staticCompositionLocalOf<(String) -> Unit> { {} }

/** Leaves taps to the image's existing click handler and scrolling to its parent. */
@Composable
fun Modifier.imageActions(imageUrl: String?): Modifier {
    if (imageUrl.isNullOrBlank()) return this
    val showActions by rememberUpdatedState(LocalImageActions.current)
    val haptics = LocalHapticFeedback.current
    val label = stringResource(Res.string.image_actions_label)
    return this
        .semantics {
            onLongClick(label) {
                showActions(imageUrl)
                true
            }
        }
        .pointerInput(imageUrl) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val longPress = awaitLongPressOrCancellation(down.id)
                if (longPress != null) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    showActions(imageUrl)
                    // Consume the release too, so a parent card cannot also open on long press.
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        event.changes.forEach { it.consume() }
                    } while (event.changes.any { it.pressed })
                }
            }
        }
}
