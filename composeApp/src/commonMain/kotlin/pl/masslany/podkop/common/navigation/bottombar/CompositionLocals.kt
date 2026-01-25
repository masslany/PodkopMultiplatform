package pl.masslany.podkop.common.navigation.bottombar

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

val LocalBottomBarManager = staticCompositionLocalOf<BottomBarManager> {
    error("BottomBarManager not provided")
}

// The connection logic
class BottomBarScrollConnection(
    private val manager: BottomBarManager
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        // We intercept the scroll here.
        // 'available.y' is the amount the user is trying to scroll.
        manager.onScroll(available.y)

        // Return Zero so we don't consume the scroll; let the LazyList have it.
        return Offset.Zero
    }
}