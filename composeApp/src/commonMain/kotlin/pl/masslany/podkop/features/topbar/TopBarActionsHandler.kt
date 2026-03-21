package pl.masslany.podkop.features.topbar

import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.features.entries.EntriesScreen
import pl.masslany.podkop.features.linksubmission.AddLinkScreen
import pl.masslany.podkop.features.notifications.NotificationsScreen
import pl.masslany.podkop.features.search.SearchScreen

class TopBarActionsHandler(private val appNavigator: AppNavigator) : TopBarActions {

    override fun onTopBarBackClicked() {
        appNavigator.back()
    }

    override fun onTopBarSearchClicked() {
        appNavigator.navigateTo(SearchScreen)
    }

    override fun onTopBarNotificationsClicked() {
        appNavigator.navigateTo(NotificationsScreen)
    }

    override fun onTopBarAddEntryClicked() {
        appNavigator.navigateTo(EntriesScreen)
    }

    override fun onTopBarAddLinkClicked() {
        appNavigator.navigateTo(AddLinkScreen)
    }
}
