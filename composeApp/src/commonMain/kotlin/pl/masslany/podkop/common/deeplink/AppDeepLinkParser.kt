package pl.masslany.podkop.common.deeplink

import io.ktor.http.Url
import io.ktor.http.parseQueryString

internal class AppDeepLinkParser {

    fun parse(rawUrl: String): AppDeepLink? {
        val normalizedUrl = rawUrl.trim()
            .takeIf { it.isNotEmpty() }
            ?.let(::normalizeUrl)
            ?: return null

        val url = runCatching { Url(normalizedUrl) }.getOrNull() ?: return null
        if (url.host.lowercase() != SUPPORTED_HOST) return null

        parseLoginCallback(url = normalizedUrl)?.let { return it }

        val segments = url.encodedPath
            .split('/')
            .filter { it.isNotBlank() }

        if (segments.size < 3) return null
        if (segments[0].lowercase() != WYKOP_SEGMENT) return null

        val detailsId = extractId(segments[2]) ?: return null

        return when (segments[1].lowercase()) {
            LINK_SEGMENT -> AppDeepLink.LinkDetails(id = detailsId)
            ENTRY_SEGMENT -> AppDeepLink.EntryDetails(id = detailsId)
            else -> null
        }
    }

    private fun parseLoginCallback(url: String): AppDeepLink.LoginCallback? {
        val query = url.substringAfter('?', missingDelimiterValue = "")
            .substringBefore('#')
        val fragment = url.substringAfter('#', missingDelimiterValue = "")

        val queryParams = parseQueryString(query)
        val fragmentParams = parseQueryString(fragment)

        val token = queryParams[TOKEN].orNonBlankFallback(fragmentParams[TOKEN]) ?: return null
        val refreshToken = queryParams[REFRESH_TOKEN].orNonBlankFallback(fragmentParams[REFRESH_TOKEN]) ?: return null

        return AppDeepLink.LoginCallback(
            token = token,
            refreshToken = refreshToken,
        )
    }

    private fun extractId(segment: String): Int? {
        return ID_PREFIX_REGEX.find(segment)
            ?.value
            ?.toIntOrNull()
    }

    private fun normalizeUrl(rawUrl: String): String {
        return if (SCHEME_SEPARATOR in rawUrl) {
            rawUrl
        } else {
            "https://$rawUrl"
        }
    }

    private fun String?.orNonBlankFallback(other: String?): String? {
        return this?.takeIf { it.isNotBlank() } ?: other?.takeIf { it.isNotBlank() }
    }

    internal companion object {
        const val TOKEN = "token"
        const val REFRESH_TOKEN = "rtoken"

        private const val SUPPORTED_HOST = "masslany.pl"
        private const val WYKOP_SEGMENT = "wykop"
        private const val LINK_SEGMENT = "link"
        private const val ENTRY_SEGMENT = "wpis"
        private const val SCHEME_SEPARATOR = "://"
        private val ID_PREFIX_REGEX = Regex("^\\d+")
    }
}
