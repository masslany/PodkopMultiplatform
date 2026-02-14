package pl.masslany.podkop.features.topbar

import pl.masslany.podkop.common.navigation.AppNavigator

class TopBarActionsHandler(private val appNavigator: AppNavigator) : TopBarActions {

    override fun onTopBarBackClicked() {
        appNavigator.back()
    }

    override fun onTopBarProfileClicked() {
        // TODO: Navigate to profile screen when it exists.
    }

    override fun onTopBarSearchClicked() {
        // TODO: Navigate to search screen when it exists.
    }
}
