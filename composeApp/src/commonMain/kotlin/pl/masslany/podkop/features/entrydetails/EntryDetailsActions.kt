package pl.masslany.podkop.features.entrydetails

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

@Stable
interface EntryDetailsActions :
    ResourceItemActions,
    PaginationActions,
    TopBarActions {

    fun onRefresh()

    fun onComposerTextChanged(content: TextFieldValue)

    fun onComposerAdultChanged(adult: Boolean)

    fun onComposerDismissed()

    fun onComposerSubmit()
}
