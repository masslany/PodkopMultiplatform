package pl.masslany.podkop.common.pagination

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch

@Composable
fun rememberLazyListPaginator(
    shouldPaginate: (lastVisibleIndex: Int?, totalItems: Int) -> Boolean,
    paginate: () -> Unit,
): LazyListState {
    val lazyListState = rememberLazyListState()
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
