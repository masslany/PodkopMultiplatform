package pl.masslany.podkop.features.rank

import androidx.compose.runtime.Stable
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface RankActions : TopBarActions {
    fun onRefresh()

    fun onUserClicked(username: String)

    /** Loads the next page; after a failed one, the list's retry button asks for it again. */
    fun paginate()
}
