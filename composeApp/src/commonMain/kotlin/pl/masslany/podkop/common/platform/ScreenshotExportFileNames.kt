package pl.masslany.podkop.common.platform

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

internal const val SCREENSHOT_PNG_MIME_TYPE = "image/png"
internal const val SCREENSHOT_PNG_FILE_EXTENSION = ".png"

private const val SCREENSHOT_FILE_NAME_MAX_LENGTH = 64
private const val SCREENSHOT_FILE_NAME_FALLBACK_PREFIX = "podkop_"
private val SCREENSHOT_FILE_NAME_INVALID_CHARS_REGEX = Regex("[^a-zA-Z0-9._-]")

internal fun sanitizeScreenshotFileName(
    fileName: String,
    fallbackDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
): String = fileName
    .replace(SCREENSHOT_FILE_NAME_INVALID_CHARS_REGEX, "_")
    .take(SCREENSHOT_FILE_NAME_MAX_LENGTH)
    .ifBlank {
        "$SCREENSHOT_FILE_NAME_FALLBACK_PREFIX$fallbackDate"
    }

internal fun toPngScreenshotFileName(
    fileName: String,
    fallbackDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
): String = buildString {
    append(sanitizeScreenshotFileName(fileName = fileName, fallbackDate = fallbackDate))
    append(SCREENSHOT_PNG_FILE_EXTENSION)
}
