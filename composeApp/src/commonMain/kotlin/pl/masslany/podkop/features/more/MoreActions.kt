package pl.masslany.podkop.features.more

import androidx.compose.runtime.Stable

@Stable
interface MoreActions {
    fun onProfileClicked()

    fun onLoginClicked()

    fun onNotificationsClicked()

    fun onMessagesClicked()

    fun onFavoritesClicked()

    fun onHitsClicked()

    fun onSearchClicked()

    fun onMyWykopClicked()

    fun onSettingsClicked()

    fun onAboutClicked()
}
