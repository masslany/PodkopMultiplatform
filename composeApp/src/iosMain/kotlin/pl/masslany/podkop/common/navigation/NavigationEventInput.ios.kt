package pl.masslany.podkop.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner

@Composable
internal actual fun rememberPlatformBackNavigationInput(enabled: Boolean): DirectNavigationEventInput? {
    if (!enabled) return null

    val dispatcherOwner = LocalNavigationEventDispatcherOwner.current ?: return null
    val input = remember(dispatcherOwner) { DirectNavigationEventInput() }

    DisposableEffect(dispatcherOwner, input) {
        dispatcherOwner.navigationEventDispatcher.addInput(input)
        onDispose {
            dispatcherOwner.navigationEventDispatcher.removeInput(input)
        }
    }

    return input
}
