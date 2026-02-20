package pl.masslany.podkop.common.pagination

sealed interface PageRequest {
    data class Index(val page: Int) : PageRequest
    data class Cursor(val key: String) : PageRequest
}

fun PageRequest.toPage(): Int? =
    when (this) {
        is PageRequest.Index -> page
        is PageRequest.Cursor -> key.toIntOrNull()
    }
