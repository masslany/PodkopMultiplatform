package pl.masslany.podkop.features.topbar

import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.profile.ProfileScreen

class TopBarActionsHandler(private val appNavigator: AppNavigator) : TopBarActions {

    override fun onTopBarBackClicked() {
        appNavigator.back()
    }

    override fun onTopBarProfileClicked() {
        appNavigator.navigateTo(ProfileScreen(username = null))
    }

    override fun onTopBarSearchClicked() {
        // TODO: Navigate to search screen when it exists.
    }
}
