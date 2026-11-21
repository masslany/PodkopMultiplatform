package pl.masslany.podkop.features.home

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.features.bottombar.BottomBarDestinationState

data class HomeScreenState(
    val destinations: ImmutableList<BottomBarDestinationState> = persistentListOf(),
    val currentTabKey: String = "",
    val currentStack: ImmutableList<NavTarget> = persistentListOf(),
)
