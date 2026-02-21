package pl.masslany.podkop.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.HomeScreen
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.features.bottombar.BottomBarDestinationState
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen

class HomeViewModel(private val appNavigator: AppNavigator, private val homeNavigator: HomeNavigator) : ViewModel() {

    val state = homeNavigator.state
        .map { navigatorState ->
            HomeScreenState(
                destinations = navigatorState.destinations.map { destination ->
                    BottomBarDestinationState(
                        screen = destination.root,
                        isSelected = navigatorState.currentTabRoot == destination.root,
                        isEnabled = destination.enabled,
                        iconRes = destination.iconRes,
                        labelRes = destination.labelRes,
                    )
                }.toImmutableList(),
                currentTabKey = navigatorState.currentTabRoot.toString(),
                currentStack = homeNavigator.currentStack(),
            )
        }
        .stateIn(viewModelScope, WhileSubscribed(5000), HomeScreenState())

    init {
        appNavigator.registerBackHandler(HomeScreen) { homeNavigator.onBack() }
    }

    fun onTabChanged(destination: NavTarget) {
        homeNavigator.onTabChanged(destination)
    }

    fun onLinkClicked(id: Int, useInlineDetails: Boolean) {
        if (useInlineDetails) {
            homeNavigator.navigateToLinkDetails(id)
        } else {
            appNavigator.navigateTo(LinkDetailsScreen(id))
        }
    }

    fun onEntryClicked(id: Int, useInlineDetails: Boolean) {
        if (useInlineDetails) {
            homeNavigator.navigateToEntryDetails(id)
        } else {
            appNavigator.navigateTo(EntryDetailsScreen(id))
        }
    }

    fun onInlineDetailsModeChanged(enabled: Boolean) {
        if (!enabled) {
            homeNavigator.detachCurrentInlineDetailsDestination()?.let { destination ->
                appNavigator.navigateTo(destination)
            }
            homeNavigator.clearInlineDetailsDestinations()
        }
    }

    fun onBackPressedInHome() {
        homeNavigator.onBack()
    }

    override fun onCleared() {
        appNavigator.unregisterBackHandler(HomeScreen)
        homeNavigator.close()
        super.onCleared()
    }
}
