package pl.masslany.podkop.features.search

import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery

internal sealed interface AdvancedSearchRequestResult {
    data class Success(val request: SearchStreamQuery) : AdvancedSearchRequestResult

    data class Error(val validationError: AdvancedSearchValidationError) : AdvancedSearchRequestResult
}

internal fun AdvancedSearchScreenState.toSearchRequest(
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): AdvancedSearchRequestResult {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) {
        return AdvancedSearchRequestResult.Error(
            validationError = AdvancedSearchValidationError.QueryRequired,
        )
    }

    val dateRange = resolveDateRange(
        state = this,
        clock = clock,
        timeZone = timeZone,
    ) ?: return AdvancedSearchRequestResult.Error(
        validationError = resolveDateRangeValidationError(this),
    )

    return AdvancedSearchRequestResult.Success(
        request = SearchStreamQuery(
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

private fun resolveDateRange(
    state: AdvancedSearchScreenState,
    clock: Clock,
    timeZone: TimeZone,
): Pair<String?, String?>? {
    if (state.datePreset != AdvancedSearchDatePreset.Custom) {
        return resolvePresetDateRange(
            preset = state.datePreset,
            clock = clock,
            timeZone = timeZone,
        )
    }

    val from = state.customDateFrom.trim().takeIf { it.isNotEmpty() }
    val to = state.customDateTo.trim().takeIf { it.isNotEmpty() }
    val parsedFrom = from?.let { parseAdvancedSearchDateTime(it) }
    val parsedTo = to?.let { parseAdvancedSearchDateTime(it) }

    if ((from != null && parsedFrom == null) || (to != null && parsedTo == null)) {
        return null
    }

    if (parsedFrom != null && parsedTo != null && parsedFrom > parsedTo) {
        return null
    }

    return from to to
}

private fun resolveDateRangeValidationError(
    state: AdvancedSearchScreenState,
): AdvancedSearchValidationError {
    val from = state.customDateFrom.trim().takeIf { it.isNotEmpty() }
    val to = state.customDateTo.trim().takeIf { it.isNotEmpty() }
    val parsedFrom = from?.let { parseAdvancedSearchDateTime(it) }
    val parsedTo = to?.let { parseAdvancedSearchDateTime(it) }

    if ((from != null && parsedFrom == null) || (to != null && parsedTo == null)) {
        return AdvancedSearchValidationError.InvalidCustomDateFormat
    }

    return AdvancedSearchValidationError.InvalidCustomDateRange
}

private fun resolvePresetDateRange(
    preset: AdvancedSearchDatePreset,
    clock: Clock,
    timeZone: TimeZone,
): Pair<String?, String?> {
    if (preset == AdvancedSearchDatePreset.AnyTime) {
        return null to null
    }

    val now = clock.now()
    val dateFrom = when (preset) {
        AdvancedSearchDatePreset.AnyTime -> null
        AdvancedSearchDatePreset.Last24Hours -> now.minus(24.hours)
        AdvancedSearchDatePreset.Last3Days -> now.minus(3.days)
        AdvancedSearchDatePreset.Last7Days -> now.minus(7.days)
        AdvancedSearchDatePreset.Last30Days -> now.minus(30.days)
        AdvancedSearchDatePreset.LastYear -> now.minus(365.days)
        AdvancedSearchDatePreset.Custom -> null
    }?.toLocalDateTime(timeZone)

    return dateFrom?.let { formatAdvancedSearchDateTime(it) } to null
}

internal fun parseSearchValues(
    rawValue: String,
    prefixToRemove: Char? = null,
): List<String> = rawValue
    .split(',', '\n', '\t', ' ')
    .asSequence()
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .map { value ->
        prefixToRemove?.let { prefix ->
            value.removePrefix(prefix.toString())
        } ?: value
    }
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .distinct()
    .toList()

internal fun parseSearchDomains(rawValue: String): List<String> = parseSearchValues(rawValue)
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

internal fun formatAdvancedSearchDateTime(value: LocalDateTime): String =
    AdvancedSearchDateTimeFormat.format(value)

internal fun parseAdvancedSearchDateTime(value: String): LocalDateTime? = runCatching {
    AdvancedSearchDateTimeFormat.parse(value)
}.getOrNull()

internal val AdvancedSearchDateTimeFormat = LocalDateTime.Format {
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
