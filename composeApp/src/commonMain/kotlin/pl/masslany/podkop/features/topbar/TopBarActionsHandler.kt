package pl.masslany.podkop.features.topbar

import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.entries.EntriesScreen

class TopBarActionsHandler(private val appNavigator: AppNavigator) : TopBarActions {

    override fun onTopBarBackClicked() {
        appNavigator.back()
    }

    override fun onTopBarAddEntryClicked() {
        appNavigator.navigateTo(EntriesScreen)
    }
}
