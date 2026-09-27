package pl.masslany.podkop.business.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import pl.masslany.podkop.business.search.domain.models.request.SearchDatePreset
import pl.masslany.podkop.business.search.domain.models.request.SearchQueryError
import pl.masslany.podkop.business.search.domain.models.request.SearchQueryInput
import pl.masslany.podkop.business.search.domain.models.request.SearchQueryResult
import pl.masslany.podkop.business.search.domain.models.request.SearchSort
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery

class SearchQueryInputTest {

    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-03-21T16:54:38Z")
    }
    private val warsaw = TimeZone.of("Europe/Warsaw")

    @Test
    fun `build normalizes every filter and resolves the preset in the given zone`() {
        val result = SearchQueryInput(
            query = " test ",
            sort = SearchSort.Newest,
            minimumVotes = 500,
            datePreset = SearchDatePreset.Last24Hours,
            tags = "#a, #a\nb",
            users = "@x\t@y",
            domains = "http://www.a.pl/x https://a.pl",
            category = "  ",
        ).build(clock, warsaw)

        assertEquals(
            SearchQueryResult.Success(
                SearchStreamQuery(
                    query = "test",
                    sort = SearchSort.Newest,
                    minimumVotes = 500,
                    dateFrom = "2026-03-20 17:54:38",
                    dateTo = null,
                    domains = listOf("a.pl"),
                    users = listOf("x", "y"),
                    tags = listOf("a", "b"),
                    category = null,
                ),
            ),
            result,
        )
    }

    @Test
    fun `custom range keeps trimmed bounds and allows an open end`() {
        val result = SearchQueryInput(
            query = "q",
            datePreset = SearchDatePreset.Custom,
            customDateFrom = " 2026-03-01 00:00:00 ",
        ).build(clock, warsaw) as SearchQueryResult.Success

        assertEquals("2026-03-01 00:00:00", result.query.dateFrom)
        assertEquals(null, result.query.dateTo)
    }

    @Test
    fun `custom values are ignored unless the custom preset is selected`() {
        val result = SearchQueryInput(
            query = "q",
            datePreset = SearchDatePreset.AnyTime,
            customDateFrom = "not a date",
        ).build(clock, warsaw) as SearchQueryResult.Success

        assertEquals(null, result.query.dateFrom)
    }

    @Test
    fun `reversed custom range is rejected`() {
        val result = SearchQueryInput(
            query = "q",
            datePreset = SearchDatePreset.Custom,
            customDateFrom = "2026-03-22 10:00:00",
            customDateTo = "2026-03-21 10:00:00",
        ).build(clock, warsaw)

        assertEquals(SearchQueryResult.Invalid(SearchQueryError.InvalidCustomDateRange), result)
    }
}
