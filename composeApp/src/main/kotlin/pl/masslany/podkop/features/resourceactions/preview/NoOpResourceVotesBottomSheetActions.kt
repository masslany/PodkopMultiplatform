package pl.masslany.podkop.features.resourceactions.preview

import pl.masslany.podkop.features.resourceactions.ResourceVotesBottomSheetActions

object NoOpResourceVotesBottomSheetActions : ResourceVotesBottomSheetActions {
    override fun onUserClicked(username: String) = Unit

    override fun onRetryClicked() = Unit

    override fun shouldPaginate(lastVisibleIndex: Int?, totalItems: Int): Boolean = false

    override fun paginate() = Unit
}
