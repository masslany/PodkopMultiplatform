package pl.masslany.podkop.features.bottombar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.NavTarget

class BottomBarViewModel(private val appNavigator: AppNavigator) :
    ViewModel(),
    BottomBarActions {

    val state = MutableStateFlow(BottomBarState.initial)

    init {
        observeNavigator()
        loadInitialDestinations()
    }

    override fun onScreenChanged(screen: NavTarget) {
        appNavigator.switchTab(screen)
    }

    private fun loadInitialDestinations() {
        val navigationState = appNavigator.state.value
        state.update {
            it.copy(
                destinations = navigationState.homeState?.availableDestinations?.map { topLevelDestination ->
                    BottomBarDestinationState(
                        screen = topLevelDestination.root,
                        isSelected = navigationState.homeState.currentTabRoot == topLevelDestination.root,
                        isEnabled = topLevelDestination.enabled,
                        labelRes = topLevelDestination.labelRes,
                        iconRes = topLevelDestination.iconRes,
                    )
                }?.toImmutableList() ?: persistentListOf(),
            )
        }
    }

    private fun observeNavigator() {
        viewModelScope.launch {
            appNavigator.state.collect {
                val selectedRoot = it.homeState?.currentTabRoot ?: return@collect

                state.update { prev ->
                    prev.copy(
                        destinations = prev.destinations.map { dest ->
                            dest.copy(isSelected = dest.screen == selectedRoot)
                        }.toImmutableList(),
                    )
                }
            }
        }
    }
}
