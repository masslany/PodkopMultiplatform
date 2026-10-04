package pl.masslany.podkop.common.deeplink

import io.ktor.http.Url
import io.ktor.http.parseQueryString

class AppDeepLinkParser {

    fun parse(rawUrl: String): AppDeepLink? {
        val normalizedUrl = rawUrl.trim()
            .takeIf { it.isNotEmpty() }
            ?.let(::normalizeUrl)
            ?: return null

        val url = runCatching { Url(normalizedUrl) }.getOrNull() ?: return null
        val scheme = url.protocol.name.lowercase()
        val host = url.host.lowercase()
        val segments = url.encodedPath
            .split('/')
            .filter { it.isNotBlank() }

        return when {
            host == SUPPORTED_HOST && scheme == "https" -> parseAppLink(url = normalizedUrl, segments = segments)
            host in CONTENT_HOSTS && scheme in CONTENT_SCHEMES -> parseContentPath(segments)
            else -> null
        }
    }

    /**
     * True for HTTPS URLs on the app's own host, including login callbacks that carry no tokens.
     * An embedded login page must not load these; it hands them to [parse] instead.
     */
    fun isAppHost(rawUrl: String): Boolean {
        val url = rawUrl.trim().takeIf { it.isNotEmpty() }
            ?.let { runCatching { Url(it) }.getOrNull() }
            ?: return false
        return url.protocol.name.lowercase() == "https" && url.host.lowercase() == SUPPORTED_HOST
    }

    /** Login callbacks are only trusted on the app's own host, never on content hosts. */
    private fun parseAppLink(url: String, segments: List<String>): AppDeepLink? {
        parseLoginCallback(url = url)?.let { return it }

        if (segments.size == 2 &&
            segments[0].lowercase() == APP_SEGMENT &&
            segments[1].lowercase() == PRIVATE_MESSAGES_SEGMENT
        ) {
            return AppDeepLink.PrivateMessagesInbox
        }

        if (segments.firstOrNull()?.lowercase() != WYKOP_SEGMENT) return null

        return parseContentPath(segments.drop(1))
    }

    /**
     * Maps a content path such as `wpis/123/slug`, `link/123/slug/komentarz/456`, `ludzie/name`
     * or `tag/name` to its screen.
     */
    private fun parseContentPath(segments: List<String>): AppDeepLink? {
        if (segments.size < 2) return null

        return when (segments[0].lowercase()) {
            LINK_SEGMENT -> extractId(segments[1])?.let { AppDeepLink.LinkDetails(id = it) }
            ENTRY_SEGMENT -> extractId(segments[1])?.let { AppDeepLink.EntryDetails(id = it) }
            PROFILE_SEGMENT -> extractName(segments[1])?.let { AppDeepLink.Profile(username = it) }
            TAG_SEGMENT -> extractTagName(segments.drop(1))?.let { AppDeepLink.Tag(name = it) }
            else -> null
        }
    }

    /** Older tag URLs put the tab first: `tag/wpisy/name` and `tag/znaleziska/name`. */
    private fun extractTagName(segments: List<String>): String? {
        val nameSegment = if (segments.size >= 2 && segments[0].lowercase() in LEGACY_TAG_TABS) {
            segments[1]
        } else {
            segments[0]
        }
        return extractName(nameSegment)?.lowercase()
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

    private fun extractId(segment: String): Int? = ID_PREFIX_REGEX.find(segment)
        ?.value
        ?.toIntOrNull()

    private fun extractName(segment: String): String? = segment.takeIf(NAME_REGEX::matches)

    private fun normalizeUrl(rawUrl: String): String = if (SCHEME_SEPARATOR in rawUrl) {
        rawUrl
    } else {
        "https://$rawUrl"
    }

    private fun String?.orNonBlankFallback(other: String?): String? = this?.takeIf {
        it.isNotBlank()
    } ?: other?.takeIf { it.isNotBlank() }

    internal companion object {
        const val TOKEN = "token"
        const val REFRESH_TOKEN = "rtoken"

        private const val SUPPORTED_HOST = "masslany.pl"
        private val CONTENT_HOSTS = setOf("wykop.pl", "www.wykop.pl", "m.wykop.pl")
        private val CONTENT_SCHEMES = setOf("https", "http")
        private const val APP_SEGMENT = "app"
        private const val PRIVATE_MESSAGES_SEGMENT = "private-messages"
        private const val WYKOP_SEGMENT = "wykop"
        private const val LINK_SEGMENT = "link"
        private const val ENTRY_SEGMENT = "wpis"
        private const val PROFILE_SEGMENT = "ludzie"
        private const val TAG_SEGMENT = "tag"
        private val LEGACY_TAG_TABS = setOf("wpisy", "znaleziska")
        private const val SCHEME_SEPARATOR = "://"
        private val ID_PREFIX_REGEX = Regex("^\\d+")
        private val NAME_REGEX = Regex("^[A-Za-z0-9_-]+$")
    }
}
