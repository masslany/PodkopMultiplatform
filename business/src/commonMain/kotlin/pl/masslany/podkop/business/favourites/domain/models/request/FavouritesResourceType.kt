package pl.masslany.podkop.business.favourites.domain.models.request

enum class FavouritesResourceType(val value: String?) {
    All(null),
    Link("link"),
    Entry("entry"),
    LinkComment("link_comment"),
    EntryComment("entry_comment"),
}
