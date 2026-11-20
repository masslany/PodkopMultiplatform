package pl.masslany.podkop.common.pagination

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
internal fun rememberLazyStaggeredGridPaginator(
    resetStateKey: String? = null,
    shouldPaginate: (lastVisibleIndex: Int?, totalItems: Int) -> Boolean,
    paginate: () -> Unit,
    lastVisibleIndexProvider: (LazyStaggeredGridState) -> Int? = { state ->
        state.layoutInfo.visibleItemsInfo.lastOrNull()?.index
    },
    totalItemsCountProvider: (LazyStaggeredGridState) -> Int = { state ->
        state.layoutInfo.totalItemsCount
    },
): LazyStaggeredGridState {
    val lazyStaggeredGridState = rememberSaveable(resetStateKey, saver = LazyStaggeredGridState.Saver) {
        LazyStaggeredGridState()
    }
    Paginator(
        paginate = paginate,
        shouldPaginate = shouldPaginate,
        lastVisibleIndexProvider = { lastVisibleIndexProvider(lazyStaggeredGridState) },
        totalItemsCountProvider = { totalItemsCountProvider(lazyStaggeredGridState) },
    )
    return lazyStaggeredGridState
}
