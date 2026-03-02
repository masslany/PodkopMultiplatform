package pl.masslany.podkop.features.topbar

import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.entries.EntriesScreen
import pl.masslany.podkop.features.profile.ProfileScreen
import pl.masslany.podkop.features.search.SearchScreen

class TopBarActionsHandler(private val appNavigator: AppNavigator) : TopBarActions {

    override fun onTopBarBackClicked() {
        appNavigator.back()
    }

    override fun onTopBarProfileClicked() {
        appNavigator.navigateTo(ProfileScreen(username = null))
    }

    override fun onTopBarSearchClicked() {
        appNavigator.navigateTo(SearchScreen)
    }

    override fun onTopBarAddEntryClicked() {
        appNavigator.navigateTo(EntriesScreen)
    }
}
