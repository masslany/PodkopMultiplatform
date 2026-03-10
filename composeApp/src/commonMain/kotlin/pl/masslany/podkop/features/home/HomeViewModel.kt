package pl.masslany.podkop.features.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.masslany.podkop.common.logging.api.AppLogger
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.HomeScreen
import pl.masslany.podkop.common.navigation.NavTarget
import pl.masslany.podkop.features.bottombar.BottomBarDestinationState
import pl.masslany.podkop.features.entrydetails.EntryDetailsScreen
import pl.masslany.podkop.features.linkdetails.LinkDetailsScreen

class HomeViewModel(
    private val appNavigator: AppNavigator,
    private val homeNavigator: HomeNavigator,
    private val logger: AppLogger,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

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
                        badgeCount = destination.badgeCount,
                    )
                }.toImmutableList(),
                currentTabKey = navigatorState.currentTabRoot.toString(),
                currentStack = homeNavigator.currentStack(),
            )
        }
        .stateIn(viewModelScope, WhileSubscribed(5000), HomeScreenState())

    init {
        homeNavigator.restoreState(savedStateHandle[HOME_NAVIGATOR_STATE_KEY])

        viewModelScope.launch {
            homeNavigator.state
                .map { homeNavigator.serializeState() }
                .distinctUntilChanged()
                .collect { serializedState ->
                    serializedState?.let {
                        savedStateHandle[HOME_NAVIGATOR_STATE_KEY] = it
                    }
                }
        }

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
        onEntryDetailsRequested(
            screen = EntryDetailsScreen.forEntry(id),
            useInlineDetails = useInlineDetails,
        )
    }

    fun onEntryReplyClicked(entryId: Int, author: String?, useInlineDetails: Boolean) {
        onEntryDetailsRequested(
            screen = EntryDetailsScreen.forEntryReply(
                entryId = entryId,
                author = author,
            ),
            useInlineDetails = useInlineDetails,
        )
    }

    fun onEntryCommentReplyClicked(
        entryId: Int,
        entryCommentId: Int,
        author: String?,
        useInlineDetails: Boolean,
    ) {
        onEntryDetailsRequested(
            screen = EntryDetailsScreen.forEntryCommentReply(
                entryId = entryId,
                entryCommentId = entryCommentId,
                author = author,
            ),
            useInlineDetails = useInlineDetails,
        )
    }

    fun onEntryDetailsRequested(screen: EntryDetailsScreen, useInlineDetails: Boolean) {
        if (useInlineDetails) {
            homeNavigator.navigateToEntryDetails(screen)
        } else {
            appNavigator.navigateTo(screen)
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

    private companion object {
        const val HOME_NAVIGATOR_STATE_KEY = "home_navigator_state"
    }
}
