package pl.masslany.podkop.business.search.domain.models.request

enum class SearchSort(val value: String) {
    Score("score"),
    Popular("popular"),
    Comments("comments"),
    Newest("newest"),
}
