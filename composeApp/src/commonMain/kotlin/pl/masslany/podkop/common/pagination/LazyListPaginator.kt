package pl.masslany.podkop.common.pagination

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch

@Composable
internal fun rememberLazyListPaginator(
    resetStateKey: Any? = null,
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

@Composable
private fun Paginator(
    paginate: () -> Unit,
    shouldPaginate: (lastVisibleIndex: Int?, totalItems: Int) -> Boolean,
    lastVisibleIndexProvider: () -> Int?,
    totalItemsCountProvider: () -> Int,
) {
    LaunchedEffect(Unit) {
        launch {
            snapshotFlow {
                shouldPaginate(lastVisibleIndexProvider(), totalItemsCountProvider())
            }.collect {
                if (it) {
                    paginate()
                }
            }
        }
    }
}
