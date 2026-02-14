package pl.masslany.podkop.features.home

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.features.bottombar.BottomBarDestinationState

data class HomeScreenState(
    val destinations: ImmutableList<BottomBarDestinationState> = persistentListOf(),
    val currentTabRoot: NavTarget? = null,
    val stacks: ImmutableMap<NavTarget, ImmutableList<NavTarget>> = persistentMapOf(),
)
