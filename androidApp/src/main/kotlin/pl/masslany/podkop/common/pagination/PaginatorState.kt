package pl.masslany.podkop.common.pagination

sealed interface PaginatorState {
    data object Idle : PaginatorState
    data object Loading : PaginatorState
    data object Exhausted : PaginatorState
    data class Error(val throwable: Throwable) : PaginatorState
}
