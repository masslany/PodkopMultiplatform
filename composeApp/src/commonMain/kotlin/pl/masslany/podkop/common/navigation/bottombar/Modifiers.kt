package pl.masslany.podkop.common.navigation.bottombar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll

@Composable
fun Modifier.autoHideBottomBar(): Modifier {
    val manager = LocalBottomBarManager.current

    // Create the connection once and remember it
    val connection = remember(manager) {
        BottomBarScrollConnection(manager)
    }

    // Attach it to the layout
    return this.nestedScroll(connection)
}