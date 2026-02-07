package pl.masslany.podkop.common.pagination

sealed interface PageRequest {
    data class Index(val page: Int) : PageRequest
    data class Cursor(val key: String) : PageRequest
}
