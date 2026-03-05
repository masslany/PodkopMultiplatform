package pl.masslany.podkop.business.hits.domain.models.request

sealed class HitsSortType(val value: String) {
    data object All : HitsSortType("all")

    data object Day : HitsSortType("day")

    data object Week : HitsSortType("week")

    data object Month : HitsSortType("month")

    data object Year : HitsSortType("year")
}
