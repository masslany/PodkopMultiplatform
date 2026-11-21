package pl.masslany.podkop.common.pagination

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
internal fun rememberLazyGridPaginator(
    resetStateKey: String,
    shouldPaginate: (lastVisibleIndex: Int?, totalItems: Int) -> Boolean,
    paginate: () -> Unit,
): LazyGridState {
    val lazyGridState = rememberSaveable(resetStateKey, saver = LazyGridState.Saver) {
        LazyGridState()
    }
    Paginator(
        paginate = paginate,
        shouldPaginate = shouldPaginate,
        lastVisibleIndexProvider = { lazyGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index },
        totalItemsCountProvider = { lazyGridState.layoutInfo.totalItemsCount },
    )
    return lazyGridState
}
