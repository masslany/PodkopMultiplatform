package pl.masslany.podkop.features.linksubmission.linkdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month

class AddLinkScreenStateTest {

    @Test
    fun `normalizeLinkTags splits on separators trims hashes lowercases and deduplicates`() {
        val actual = normalizeLinkTags(" #Polityka,AI\nai   #Tech ")

        assertEquals(listOf("polityka", "ai", "tech"), actual)
    }

    @Test
    fun `mergeLinkTags preserves existing order and appends only new tags`() {
        val actual = mergeLinkTags(
            existing = listOf("polityka", "ai"),
            additions = listOf("ai", "tech", "nauka"),
        )

        assertEquals(listOf("polityka", "ai", "tech", "nauka"), actual)
    }

    @Test
    fun `normalizeLinkTagQuery trims hash and lowercases`() {
        val actual = normalizeLinkTagQuery("  #Szczecin ")

        assertEquals("szczecin", actual)
    }

    @Test
    fun `formatAddLinkAbsoluteDate returns expected presentation`() {
        val actual = formatAddLinkAbsoluteDate(
            LocalDateTime(
                year = 2026,
                month = Month.JANUARY,
                day = 5,
                hour = 13,
                minute = 12,
                second = 43,
                nanosecond = 0,
            ),
        )

        assertEquals("05.01.2026, 13:12", actual)
    }

    @Test
    fun `formatAddLinkAbsoluteDate returns dash for missing value`() {
        assertEquals("-", formatAddLinkAbsoluteDate(null))
    }
}
