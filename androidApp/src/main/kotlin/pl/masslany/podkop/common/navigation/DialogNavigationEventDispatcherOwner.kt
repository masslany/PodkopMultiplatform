package pl.masslany.podkop.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.findViewTreeNavigationEventDispatcherOwner

/**
 * The owner of the Back events sent to the dialog window this is composed in. A dialog is its own
 * window, so its Back events never reach the dispatcher of the screen that opened it. The
 * `ComponentDialog` behind a Compose `Dialog` registers itself on the window's view tree.
 */
@Composable
internal fun dialogNavigationEventDispatcherOwner(): NavigationEventDispatcherOwner? =
    LocalView.current.findViewTreeNavigationEventDispatcherOwner()
