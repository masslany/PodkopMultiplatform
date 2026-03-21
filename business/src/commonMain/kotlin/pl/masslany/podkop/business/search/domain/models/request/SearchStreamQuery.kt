package pl.masslany.podkop.business.search.domain.models.request

data class SearchStreamQuery(
    val query: String,
    val sort: SearchSort = SearchSort.Score,
    val minimumVotes: Int? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val domains: List<String> = emptyList(),
    val users: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val category: String? = null,
)
