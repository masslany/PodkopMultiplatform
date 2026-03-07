package pl.masslany.podkop.features.privatemessages.models

data class NewConversationScreenState(val username: String, val suggestions: UserSuggestionsState) {
    val normalizedUsername: String = username.normalizePrivateMessageUsername()

    companion object {
        val initial = NewConversationScreenState(
            username = "",
            suggestions = UserSuggestionsState.initial,
        )
    }
}
