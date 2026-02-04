package pl.masslany.podkop.common.navigation.bottombar

import androidx.compose.runtime.Stable
import pl.masslany.podkop.common.navigation.AppNavigator

@Stable
class BottomBarManager(private val navigator: AppNavigator) {
    // Buffer to prevent flickering. User must scroll X pixels before we react.
    private val scrollThreshold = 15f
    private var accumulatedScroll = 0f

    fun onScroll(delta: Float) {
        // delta > 0: Scrolling Up (Content moves down) -> Show Bar
        // delta < 0: Scrolling Down (Content moves up) -> Hide Bar

        // 1. Add to accumulator
        accumulatedScroll += delta

        // 2. Check thresholds
        if (accumulatedScroll < -scrollThreshold) {
            // Scrolling Down -> Hide
            navigator.setBottomBarVisible(false)
            accumulatedScroll = 0f
        } else if (accumulatedScroll > scrollThreshold) {
            // Scrolling Up -> Show
            navigator.setBottomBarVisible(true)
            accumulatedScroll = 0f
        }
    }

    // Optional: Helper to force show (e.g., when reaching top of list)
    fun show() {
        navigator.setBottomBarVisible(true)
        accumulatedScroll = 0f
    }
}
