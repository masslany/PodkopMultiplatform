package pl.masslany.podkop.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.findViewTreeNavigationEventDispatcherOwner

/** The `ComponentDialog` behind a Compose `Dialog` registers itself on the window's view tree. */
@Composable
internal actual fun dialogNavigationEventDispatcherOwner(): NavigationEventDispatcherOwner? =
    LocalView.current.findViewTreeNavigationEventDispatcherOwner()
