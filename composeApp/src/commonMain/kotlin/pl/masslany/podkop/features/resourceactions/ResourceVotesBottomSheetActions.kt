package pl.masslany.podkop.features.resourceactions

import pl.masslany.podkop.common.pagination.PaginationActions

interface ResourceVotesBottomSheetActions : PaginationActions {
    fun onUserClicked(username: String)
    fun onRetryClicked()
}
