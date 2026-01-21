package pl.masslany.podkop.business.links.domain.models.request

sealed class LinksSortType(val value: String) {
    data object Newest : LinksSortType("newest")

    data object Active : LinksSortType("active")

    data object Commented : LinksSortType("commented")

    data object Digged : LinksSortType("digged")
}
