package pl.masslany.podkop.features.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import pl.masslany.podkop.business.search.domain.models.request.SearchSort

class AdvancedSearchRequestBuilderTest {

    @Test
    fun `toSearchRequest builds query with parsed filters and preset date`() {
        val state = AdvancedSearchScreenState.initial.copy(
            query = " test ",
            sort = SearchSort.Comments,
            minimumVotes = 100,
            datePreset = AdvancedSearchDatePreset.Last7Days,
            tags = "#android kotlin",
            users = "@wykop observer",
            domains = "https://www.wykop.pl/path onet.pl",
            category = " technologia ",
        )

        val result = state.toSearchRequest(
            clock = FixedClock(Instant.parse("2026-03-21T16:54:38Z")),
            timeZone = TimeZone.of("Europe/Warsaw"),
        )

        assertTrue(result is AdvancedSearchRequestResult.Success)
        assertEquals("test", result.request.query)
        assertEquals(SearchSort.Comments, result.request.sort)
        assertEquals(100, result.request.minimumVotes)
        assertEquals("2026-03-14 17:54:38", result.request.dateFrom)
        assertEquals(null, result.request.dateTo)
        assertEquals(listOf("android", "kotlin"), result.request.tags)
        assertEquals(listOf("wykop", "observer"), result.request.users)
        assertEquals(listOf("wykop.pl", "onet.pl"), result.request.domains)
        assertEquals("technologia", result.request.category)
    }

    @Test
    fun `toSearchRequest returns query required error when query is blank`() {
        val result = AdvancedSearchScreenState.initial.copy(
            query = "   ",
        ).toSearchRequest()

        assertEquals(
            AdvancedSearchRequestResult.Error(
                validationError = AdvancedSearchValidationError.QueryRequired,
            ),
            result,
        )
    }

    @Test
    fun `toSearchRequest returns invalid custom date format error`() {
        val result = AdvancedSearchScreenState.initial.copy(
            query = "test",
            datePreset = AdvancedSearchDatePreset.Custom,
            customDateFrom = "2026-13-99 99:99:99",
        ).toSearchRequest()

        assertEquals(
            AdvancedSearchRequestResult.Error(
                validationError = AdvancedSearchValidationError.InvalidCustomDateFormat,
            ),
            result,
        )
    }

    @Test
    fun `toSearchRequest returns invalid custom date range error`() {
        val result = AdvancedSearchScreenState.initial.copy(
            query = "test",
            datePreset = AdvancedSearchDatePreset.Custom,
            customDateFrom = "2026-03-22 10:00:00",
            customDateTo = "2026-03-21 10:00:00",
        ).toSearchRequest()

        assertEquals(
            AdvancedSearchRequestResult.Error(
                validationError = AdvancedSearchValidationError.InvalidCustomDateRange,
            ),
            result,
        )
    }
}

private class FixedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}
