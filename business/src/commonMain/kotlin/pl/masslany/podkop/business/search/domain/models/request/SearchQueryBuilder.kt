package pl.masslany.podkop.business.search.domain.models.request

import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import pl.masslany.podkop.business.common.domain.models.common.Resources

enum class SearchDatePreset {
    AnyTime,
    Last24Hours,
    Last3Days,
    Last7Days,
    Last30Days,
    LastYear,
    Custom,
}

enum class SearchQueryError {
    QueryRequired,
    InvalidCustomDateFormat,
    InvalidCustomDateRange,
}

sealed interface SearchQueryResult {
    data class Success(val query: SearchStreamQuery) : SearchQueryResult

    data class Invalid(val error: SearchQueryError) : SearchQueryResult
}

/** Raw advanced search form values; normalization and validation live in [build]. */
data class SearchQueryInput(
    val query: String,
    val sort: SearchSort = SearchSort.Score,
    val minimumVotes: Int? = null,
    val datePreset: SearchDatePreset = SearchDatePreset.AnyTime,
    val customDateFrom: String = "",
    val customDateTo: String = "",
    val tags: String = "",
    val users: String = "",
    val domains: String = "",
    val category: String = "",
) {
    fun build(
        clock: Clock = Clock.System,
        timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): SearchQueryResult {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            return SearchQueryResult.Invalid(SearchQueryError.QueryRequired)
        }

        val dateRange = when (datePreset) {
            SearchDatePreset.Custom -> when (val range = customDateRange()) {
                is CustomRange.Valid -> range.from to range.to
                is CustomRange.Invalid -> return SearchQueryResult.Invalid(range.error)
            }
            else -> presetDateFrom(clock, timeZone) to null
        }

        return SearchQueryResult.Success(
            SearchStreamQuery(
                query = normalizedQuery,
                sort = sort,
                minimumVotes = minimumVotes,
                dateFrom = dateRange.first,
                dateTo = dateRange.second,
                domains = parseSearchDomains(domains),
                users = parseSearchValues(users, '@'),
                tags = parseSearchValues(tags, '#'),
                category = category.trim().takeIf { it.isNotEmpty() },
            ),
        )
    }

    private sealed interface CustomRange {
        data class Valid(val from: String?, val to: String?) : CustomRange
        data class Invalid(val error: SearchQueryError) : CustomRange
    }

    private fun customDateRange(): CustomRange {
        val from = customDateFrom.trim().takeIf { it.isNotEmpty() }
        val to = customDateTo.trim().takeIf { it.isNotEmpty() }
        val parsedFrom = from?.let(::parseSearchDateTime)
        val parsedTo = to?.let(::parseSearchDateTime)

        if ((from != null && parsedFrom == null) || (to != null && parsedTo == null)) {
            return CustomRange.Invalid(SearchQueryError.InvalidCustomDateFormat)
        }
        if (parsedFrom != null && parsedTo != null && parsedFrom > parsedTo) {
            return CustomRange.Invalid(SearchQueryError.InvalidCustomDateRange)
        }
        return CustomRange.Valid(from, to)
    }

    private fun presetDateFrom(clock: Clock, timeZone: TimeZone): String? {
        val now = clock.now()
        val from = when (datePreset) {
            SearchDatePreset.AnyTime, SearchDatePreset.Custom -> return null
            SearchDatePreset.Last24Hours -> now.minus(24.hours)
            SearchDatePreset.Last3Days -> now.minus(3.days)
            SearchDatePreset.Last7Days -> now.minus(7.days)
            SearchDatePreset.Last30Days -> now.minus(30.days)
            SearchDatePreset.LastYear -> now.minus(365.days)
        }
        return SearchDateTimeFormat.format(from.toLocalDateTime(timeZone))
    }
}

fun parseSearchValues(
    rawValue: String,
    prefixToRemove: Char? = null,
): List<String> = rawValue
    .split(',', '\n', '\t', ' ')
    .asSequence()
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .map { value -> prefixToRemove?.let { value.removePrefix(it.toString()) } ?: value }
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .distinct()
    .toList()

fun parseSearchDomains(rawValue: String): List<String> = parseSearchValues(rawValue)
    .map { value ->
        value
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore('/')
            .removePrefix("www.")
            .trim()
    }
    .filter { it.isNotEmpty() }
    .distinct()

fun parseSearchDateTime(value: String): LocalDateTime? = runCatching {
    SearchDateTimeFormat.parse(value)
}.getOrNull()

val SearchDateTimeFormat = LocalDateTime.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    day()
    char(' ')
    hour()
    char(':')
    minute()
    char(':')
    second()
}

/**
 * The search endpoint may return a blank `next` while more results exist; probe the next
 * numbered page until the reported total is reached.
 */
fun Resources.withSearchFallbackPagination(
    currentItemCount: Int,
    currentPage: Int,
): Resources {
    val currentPagination = pagination ?: return this
    val totalItemsAfterAppend = currentItemCount + data.size
    val shouldProbeNextPage = currentPagination.next.isBlank() &&
        currentPagination.total > 0 &&
        data.isNotEmpty() &&
        totalItemsAfterAppend < currentPagination.total

    if (!shouldProbeNextPage) {
        return this
    }

    return copy(
        pagination = currentPagination.copy(next = (currentPage + 1).toString()),
    )
}
