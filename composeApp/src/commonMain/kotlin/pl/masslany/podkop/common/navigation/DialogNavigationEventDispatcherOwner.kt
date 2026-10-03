package pl.masslany.podkop.common.navigation

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventDispatcherOwner

/**
 * The owner of the Back events sent to the dialog window this is composed in. A dialog is its own
 * window, so its Back events never reach the dispatcher of the screen that opened it.
 */
@Composable
internal expect fun dialogNavigationEventDispatcherOwner(): NavigationEventDispatcherOwner?
