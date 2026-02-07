package pl.masslany.podkop.common.pagination

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources

class Paginator(
    private val scope: CoroutineScope,
    private val onNewItems: (items: List<ResourceItem>) -> Unit,
    private val loader: suspend (PageRequest) -> Result<Resources>,
) {

    private val _state = MutableStateFlow<PaginatorState>(PaginatorState.Idle)
    val state = _state.asStateFlow()

    // First page is loaded before setting up the pagination
    private var nextPage = 2
    private var nextCursor: String? = null
    private var total: Int? = null
    private var emittedCount = 0

    fun setup(pagination: Pagination?, initialItemCount: Int) {
        nextPage = 2
        nextCursor = pagination?.next
        total = pagination?.total
        emittedCount = initialItemCount
        _state.value = PaginatorState.Idle
    }

    fun shouldPaginate(
        lastVisibleIndex: Int?,
        totalItemsCount: Int,
        prefetchDistance: Int = 8,
    ): Boolean {
        if (_state.value != PaginatorState.Idle) return false
        if (lastVisibleIndex == null) return false
        if (totalItemsCount >= (total ?: Int.MAX_VALUE)) return false

        return lastVisibleIndex + prefetchDistance >= totalItemsCount
    }

    fun paginate() {
        _state.value = PaginatorState.Loading

        scope.launch {
            val currentNextCursor = nextCursor
            val request = if (!currentNextCursor.isNullOrEmpty()) {
                PageRequest.Cursor(currentNextCursor)
            } else {
                PageRequest.Index(nextPage)
            }

            loader(request)
                .onSuccess { resources ->
                    nextCursor = resources.pagination?.next
                    total = resources.pagination?.total
                    nextPage++

                    onNewItems(resources.data)

                    _state.value =
                        if (total != null && emittedCount >= (total ?: Int.MAX_VALUE)) {
                            PaginatorState.Exhausted
                        } else {
                            PaginatorState.Idle
                        }
                }
                .onFailure {
                    _state.value = PaginatorState.Error(it)
                }
        }
    }
}
