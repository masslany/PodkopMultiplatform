package pl.masslany.podkop.common.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class ScreenshotExportFileNamesTest {

    @Test
    fun `sanitizeScreenshotFileName replaces unsupported characters`() {
        val result = sanitizeScreenshotFileName(
            fileName = "entry/123 test#tag",
            fallbackDate = LocalDate(2026, 2, 26),
        )

        assertEquals("entry_123_test_tag", result)
    }

    @Test
    fun `sanitizeScreenshotFileName trims length to max`() {
        val result = sanitizeScreenshotFileName(
            fileName = "a".repeat(96),
            fallbackDate = LocalDate(2026, 2, 26),
        )

        assertEquals(64, result.length)
        assertTrue(result.all { it == 'a' })
    }

    @Test
    fun `sanitizeScreenshotFileName falls back for blank input`() {
        val result = sanitizeScreenshotFileName(
            fileName = "",
            fallbackDate = LocalDate(2026, 2, 26),
        )

        assertEquals("podkop_2026-02-26", result)
    }

    @Test
    fun `toPngScreenshotFileName appends png extension`() {
        val result = toPngScreenshotFileName(
            fileName = "comment 123",
            fallbackDate = LocalDate(2026, 2, 26),
        )

        assertEquals("comment_123.png", result)
    }
}
