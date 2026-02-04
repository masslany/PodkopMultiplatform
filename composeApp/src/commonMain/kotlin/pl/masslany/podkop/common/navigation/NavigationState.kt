package pl.masslany.podkop.common.navigation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf

sealed interface OverlayState {
    data object None : OverlayState
    data class Blocking(val target: OverlayTarget) : OverlayState
}

data class TabState(
    val availableTabs: ImmutableList<TopLevelDestination>,
    val currentTabRoot: NavTarget,
    val stacks: ImmutableMap<NavTarget, ImmutableList<NavTarget>>, // Key: Tab Root, Value: Backstack
)

data class NavigationState(
    // The Global Stack. Holds Login, Onboarding, OR the MainAppTarget.
    val rootStack: ImmutableList<NavTarget> = persistentListOf(),

    // Logic for Tabs (Only active if rootStack.last() is MainAppTarget)
    val tabState: TabState? = null,

    // Overlays sit on top of everything
    val overlay: OverlayState = OverlayState.None,

    val isBottomBarVisible: Boolean = true,
) {
    val isTabMode: Boolean get() = tabState != null
}
