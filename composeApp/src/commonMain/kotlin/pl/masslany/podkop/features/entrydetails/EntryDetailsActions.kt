package pl.masslany.podkop.features.entrydetails

import androidx.compose.runtime.Stable
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.features.resources.ResourceItemActions

@Stable
interface EntryDetailsActions :
    ResourceItemActions,
    PaginationActions {

    fun onRefresh()
}
