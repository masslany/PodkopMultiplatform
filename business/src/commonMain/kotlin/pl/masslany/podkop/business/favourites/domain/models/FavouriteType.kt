package pl.masslany.podkop.business.favourites.domain.models

enum class FavouriteType(val value: String) {
    Link("link"),
    LinkComment("link_comment"),
    Entry("entry"),
    EntryComment("entry_comment"),
}
