package pl.masslany.podkop.business.hits.domain.models.request

sealed class HitsSortType(val value: String) {
    data object Day : HitsSortType("day")
}
