package pl.masslany.podkop.business.entries.domain.models.request

sealed class EntriesSortType(val value: String) {
    data object Newest : EntriesSortType("newest")

    data object Active : EntriesSortType("active")

    data object Hot : EntriesSortType("hot")
}
