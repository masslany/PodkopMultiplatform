package pl.masslany.podkop.features.entrydetails.preview

import pl.masslany.podkop.common.pagination.PaginationActions
import pl.masslany.podkop.common.preview.NoOpPaginationActions
import pl.masslany.podkop.common.preview.NoOpResourceItemActions
import pl.masslany.podkop.common.preview.NoOpTopBarActions
import pl.masslany.podkop.features.entrydetails.EntryDetailsActions
import pl.masslany.podkop.features.resources.ResourceItemActions
import pl.masslany.podkop.features.topbar.TopBarActions

object NoOpEntryDetailsActions :
    EntryDetailsActions,
    ResourceItemActions by NoOpResourceItemActions,
    PaginationActions by NoOpPaginationActions,
    TopBarActions by NoOpTopBarActions {
    override fun onRefresh() = Unit
    override fun onComposerTextChanged(content: String) = Unit
    override fun onComposerDismissed() = Unit
    override fun onComposerSubmit() = Unit
}
