package pl.masslany.podkop.features.blacklists.models

data class BlacklistedTagSuggestionItemState(val name: String, val followers: Int) : BlacklistSuggestionItemState {
    override val key: String = "tag:$name"
}
