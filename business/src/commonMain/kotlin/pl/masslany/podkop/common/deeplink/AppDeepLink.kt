package pl.masslany.podkop.common.deeplink

sealed interface AppDeepLink {
    data class LoginCallback(val token: String, val refreshToken: String) : AppDeepLink

    data class LinkDetails(val id: Int) : AppDeepLink

    data class EntryDetails(val id: Int) : AppDeepLink

    data class Profile(val username: String) : AppDeepLink

    data class Tag(val name: String) : AppDeepLink

    data object PrivateMessagesInbox : AppDeepLink
}
