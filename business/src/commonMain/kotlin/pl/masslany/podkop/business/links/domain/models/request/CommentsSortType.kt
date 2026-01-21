package pl.masslany.podkop.business.links.domain.models.request

sealed class CommentsSortType(val value: String) {
    data object Newest : CommentsSortType("newest")

    data object Best : CommentsSortType("best")

    data object Oldest : CommentsSortType("oldest")
}
