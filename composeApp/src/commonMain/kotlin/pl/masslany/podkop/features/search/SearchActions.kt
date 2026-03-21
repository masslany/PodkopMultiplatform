package pl.masslany.podkop.features.search

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface SearchActions : TopBarActions {
    fun onQueryChanged(value: String)

    fun onAdvancedSearchClicked()

    fun onTagClicked(tag: String)

    fun onUserClicked(username: String)

    fun onRetryClicked()
}
