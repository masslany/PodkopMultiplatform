package pl.masslany.podkop.features.search

import androidx.compose.runtime.Stable
import pl.masslany.podkop.business.search.domain.models.request.SearchSort
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface AdvancedSearchActions :
    TopBarActions,
    ResourceItemActions {
    fun onQueryChanged(value: String)

    fun onSortSelected(sort: SearchSort)

    fun onMinimumVotesSelected(value: Int?)

    fun onDatePresetSelected(preset: AdvancedSearchDatePreset)

    fun onCustomDateFromChanged(value: String)

    fun onCustomDateToChanged(value: String)

    fun onTagsChanged(value: String)

    fun onUsersChanged(value: String)

    fun onDomainsChanged(value: String)

    fun onCategoryChanged(value: String)

    fun onSearchClicked()

    fun onRefresh()
}
