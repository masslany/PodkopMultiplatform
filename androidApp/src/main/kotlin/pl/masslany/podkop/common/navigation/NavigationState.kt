package pl.masslany.podkop.common.navigation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

sealed interface OverlayState {
    data object None : OverlayState
    data class Blocking(val target: OverlayTarget) : OverlayState
}

data class NavigationState(
    // The Global Stack. Holds screens in a single linear stack, e.g. HomeScreen -> EntryDetailsScreen.
    val rootStack: ImmutableList<NavTarget> = persistentListOf(),

    // Overlays sit on top of everything
    val overlay: OverlayState = OverlayState.None,
)
