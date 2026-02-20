package pl.masslany.podkop.business.tags.domain.models.request

sealed class TagsSort(val value: String) {
    data object All : TagsSort("all")
    data object Best : TagsSort("best")
}
