package pl.masslany.podkop.business.embeds.data.main.token

import kotlin.math.PI
import kotlin.math.floor

/**
 * Generates the best-effort `token` query parameter accepted by Twitter/X syndication endpoint.
 *
 * This is not an official API contract. The repository still retries without a token when requests
 * fail, because endpoint behavior may change.
 */
internal interface TwitterSyndicationTokenGenerator {
    fun generate(tweetId: Long): String
}

internal class TwitterSyndicationTokenGeneratorImpl : TwitterSyndicationTokenGenerator {
    override fun generate(tweetId: Long): String {
        val value = (tweetId.toDouble() / 1_000_000_000_000_000.0) * PI
        return jsBase36(value)
            .replace(".", "")
            .replace("0", "")
    }
}

/**
 * Minimal Kotlin implementation of JS-style base36 conversion for positive doubles.
 *
 * Needed because the token generation logic is usually described with JavaScript `toString(36)`
 * semantics, while this code runs in KMP common code.
 */
private fun jsBase36(value: Double): String {
    if (!value.isFinite() || value <= 0.0) return "0"

    val integerPart = floor(value).toLong()
    var fractionalPart = value - floor(value)
    val builder = StringBuilder()
    builder.append(integerPart.toBase36String())

    if (fractionalPart <= 0.0) {
        return builder.toString()
    }

    builder.append('.')
    var remainingDigits = 18
    while (remainingDigits > 0) {
        fractionalPart *= 36.0
        val digit = floor(fractionalPart).toInt().coerceIn(0, 35)
        builder.append(Base36Digits[digit])
        fractionalPart -= digit.toDouble()
        if (fractionalPart <= 0.0) {
            break
        }
        remainingDigits--
    }
    return builder.toString()
}

private fun Long.toBase36String(): String {
    if (this == 0L) return "0"

    var value = this
    val chars = StringBuilder()
    while (value > 0L) {
        val digit = (value % 36L).toInt()
        chars.append(Base36Digits[digit])
        value /= 36L
    }
    return chars.reverse().toString()
}

private const val Base36Digits = "0123456789abcdefghijklmnopqrstuvwxyz"
