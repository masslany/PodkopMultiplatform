package pl.masslany.podkop.features.bottombar

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class BottomBarState(val destinations: ImmutableList<BottomBarDestinationState>) {

    companion object {
        val initial = BottomBarState(
            destinations = persistentListOf(),
        )
    }
}
