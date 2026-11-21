package pl.masslany.podkop.common.pagination

interface PaginationActions {
    fun shouldPaginate(lastVisibleIndex: Int?, totalItems: Int): Boolean
    fun paginate()
}
