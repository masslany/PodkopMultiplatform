package pl.masslany.podkop.business.embeds.data.main.parser

import kotlinx.serialization.json.JsonObject
import pl.masslany.podkop.business.common.data.network.json.getArray
import pl.masslany.podkop.business.common.data.network.json.getInt
import pl.masslany.podkop.business.common.data.network.json.getObject
import pl.masslany.podkop.business.common.data.network.json.getString
import pl.masslany.podkop.business.common.data.network.json.jsonObjectOrNull
import pl.masslany.podkop.business.embeds.domain.models.TwitterEmbedPreview

/**
 * Parser for X/Twitter syndication payloads returned by `cdn.syndication.twimg.com/tweet-result`.
 *
 * The payload schema is unofficial and can drift. This parser is intentionally defensive and only
 * extracts the subset required for inline tweet preview rendering.
 *
 * Tolerance rules:
 * - supports classic `text` and long-form `note_tweet` payloads
 * - treats missing counters as zero
 * - only requires author handle + non-empty text to produce a usable result
 */
internal interface TwitterSyndicationParser {
    fun parseTweetPreview(root: JsonObject): TwitterEmbedPreview?
}

internal class TwitterSyndicationParserImpl : TwitterSyndicationParser {
    override fun parseTweetPreview(root: JsonObject): TwitterEmbedPreview? {
        val user = root.getObject("user") ?: return null
        val text = root.extractTweetText()
            ?.decodeHtmlEntities()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null
        val authorHandle = user.getString("screen_name")
            ?.removePrefix("@")
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val photo = root.getArray("photos")
            ?.firstOrNull()
            ?.jsonObjectOrNull()

        val photoWidth = photo?.getInt("width")
        val photoHeight = photo?.getInt("height")
        val aspectRatio = if (photoWidth != null && photoHeight != null && photoWidth > 0 && photoHeight > 0) {
            photoWidth.toFloat() / photoHeight.toFloat()
        } else {
            null
        }

        return TwitterEmbedPreview(
            authorName = user.getString("name").orEmpty(),
            authorHandle = authorHandle,
            avatarUrl = user.getString("profile_image_url_https"),
            text = text,
            replyCount = root.getInt("reply_count") ?: root.getInt("conversation_count") ?: 0,
            retweetCount = root.getInt("retweet_count") ?: 0,
            likeCount = root.getInt("favorite_count") ?: 0,
            mediaThumbnailUrl = photo?.getString("url"),
            mediaAspectRatio = aspectRatio,
        )
    }
}

private fun JsonObject.extractTweetText(): String? {
    getString("text")?.let { return it }

    return getObject("note_tweet")
        ?.getObject("note_tweet_results")
        ?.getObject("result")
        ?.getString("text")
}

private fun String.decodeHtmlEntities(): String = this
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
