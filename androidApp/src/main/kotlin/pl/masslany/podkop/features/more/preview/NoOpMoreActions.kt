package pl.masslany.podkop.features.more.preview

import pl.masslany.podkop.features.more.MoreActions

object NoOpMoreActions : MoreActions {
    override fun onProfileClicked() = Unit

    override fun onLoginClicked() = Unit

    override fun onNotificationsClicked() = Unit

    override fun onMessagesClicked() = Unit

    override fun onFavoritesClicked() = Unit

    override fun onHitsClicked() = Unit

    override fun onSearchClicked() = Unit

    override fun onRankClicked() = Unit

    override fun onMyWykopClicked() = Unit

    override fun onSettingsClicked() = Unit

    override fun onAboutClicked() = Unit
}
