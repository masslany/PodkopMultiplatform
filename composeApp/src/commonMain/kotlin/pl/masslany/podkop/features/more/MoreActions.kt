package pl.masslany.podkop.features.more

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.more.models.MoreSectionItemType

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

    fun onSectionItemClicked(type: MoreSectionItemType) {
        when (type) {
            MoreSectionItemType.Notifications -> onNotificationsClicked()
            MoreSectionItemType.Messages -> onMessagesClicked()
            MoreSectionItemType.Favorites -> onFavoritesClicked()
            MoreSectionItemType.Hits -> onHitsClicked()
            MoreSectionItemType.Search -> onSearchClicked()
            MoreSectionItemType.MyWykop -> onMyWykopClicked()
            MoreSectionItemType.Settings -> onSettingsClicked()
            MoreSectionItemType.About -> onAboutClicked()
        }
    }
}
