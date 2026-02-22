package pl.masslany.podkop.common.deeplink

internal sealed interface AppDeepLink {
    data class LoginCallback(
        val token: String,
        val refreshToken: String,
    ) : AppDeepLink

    data class LinkDetails(val id: Int) : AppDeepLink

    data class EntryDetails(val id: Int) : AppDeepLink
}
