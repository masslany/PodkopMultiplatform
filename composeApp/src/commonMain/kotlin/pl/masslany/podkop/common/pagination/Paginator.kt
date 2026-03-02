package pl.masslany.podkop.common.pagination

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.masslany.podkop.business.common.domain.models.common.PaginatedData
import pl.masslany.podkop.business.common.domain.models.common.Pagination

class Paginator<T>(
    private val scope: CoroutineScope,
    private val onNewItems: suspend (items: List<T>) -> Unit,
    private val onError: suspend (Throwable) -> Unit = {},
    private val loader: suspend (PageRequest) -> Result<PaginatedData<T>>,
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
        total = pagination?.total?.takeIf { it > 0 }
        emittedCount = initialItemCount
        val noMoreItems =
            initialItemCount == 0 &&
                pagination != null &&
                pagination.next.isBlank()
        _state.value = if (noMoreItems) {
            PaginatorState.Exhausted
        } else {
            PaginatorState.Idle
        }
    }

    fun shouldPaginate(
        lastVisibleIndex: Int?,
        totalItemsCount: Int,
        prefetchDistance: Int = 8,
    ): Boolean {
        if (_state.value != PaginatorState.Idle) return false
        if (lastVisibleIndex == null) return false
        if (total != null && emittedCount >= (total ?: Int.MAX_VALUE)) return false

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
                    total = resources.pagination?.total?.takeIf { it > 0 }
                    nextPage++

                    emittedCount += resources.data.size
                    onNewItems(resources.data)

                    val hasNoNextCursor = resources.pagination?.next.isNullOrBlank()
                    val noMoreItems = resources.data.isEmpty() && hasNoNextCursor
                    _state.value =
                        if (noMoreItems || (total != null && emittedCount >= (total ?: Int.MAX_VALUE))) {
                            PaginatorState.Exhausted
                        } else {
                            PaginatorState.Idle
                        }
                }
                .onFailure {
                    _state.value = PaginatorState.Error(it)
                    onError(it)
                }
        }
    }
}

@Composable
fun Paginator(
    paginate: () -> Unit,
    shouldPaginate: (lastVisibleIndex: Int?, totalItems: Int) -> Boolean,
    lastVisibleIndexProvider: () -> Int?,
    totalItemsCountProvider: () -> Int,
) {
    val currentPaginate by rememberUpdatedState(newValue = paginate)
    val currentShouldPaginate by rememberUpdatedState(newValue = shouldPaginate)
    val currentLastVisibleIndexProvider by rememberUpdatedState(newValue = lastVisibleIndexProvider)
    val currentTotalItemsCountProvider by rememberUpdatedState(newValue = totalItemsCountProvider)

    LaunchedEffect(Unit) {
        launch {
            snapshotFlow {
                currentShouldPaginate(
                    currentLastVisibleIndexProvider(),
                    currentTotalItemsCountProvider(),
                )
            }.collect {
                if (it) {
                    currentPaginate()
                }
            }
        }
    }
}
