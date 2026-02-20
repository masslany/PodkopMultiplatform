package pl.masslany.podkop.business.tags.domain.models.request

sealed class TagsType(val value: String) {
    data object All : TagsType("all")
    data object Links : TagsType("link")
    data object Entries : TagsType("entry")
}
