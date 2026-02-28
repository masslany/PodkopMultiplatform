package pl.masslany.podkop.common.pagination

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
internal fun rememberLazyListPaginator(
    resetStateKey: String? = null,
    shouldPaginate: (lastVisibleIndex: Int?, totalItems: Int) -> Boolean,
    paginate: () -> Unit,
): LazyListState {
    val lazyListState = rememberSaveable(resetStateKey, saver = LazyListState.Saver) {
        LazyListState()
    }
    Paginator(
        paginate = paginate,
        shouldPaginate = shouldPaginate,
        lastVisibleIndexProvider = { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index },
        totalItemsCountProvider = { lazyListState.layoutInfo.totalItemsCount },
    )
    return lazyListState
}
