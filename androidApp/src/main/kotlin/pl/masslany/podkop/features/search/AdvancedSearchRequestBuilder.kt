package pl.masslany.podkop.features.search

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import pl.masslany.podkop.business.search.domain.models.request.SearchQueryInput
import pl.masslany.podkop.business.search.domain.models.request.SearchQueryResult
import pl.masslany.podkop.business.search.domain.models.request.SearchStreamQuery

internal sealed interface AdvancedSearchRequestResult {
    data class Success(val request: SearchStreamQuery) : AdvancedSearchRequestResult

    data class Error(val validationError: AdvancedSearchValidationError) : AdvancedSearchRequestResult
}

internal fun AdvancedSearchScreenState.toSearchRequest(
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): AdvancedSearchRequestResult = when (
    val result = SearchQueryInput(
        query = query,
        sort = sort,
        minimumVotes = minimumVotes,
        datePreset = datePreset,
        customDateFrom = customDateFrom,
        customDateTo = customDateTo,
        tags = tags,
        users = users,
        domains = domains,
        category = category,
    ).build(clock = clock, timeZone = timeZone)
) {
    is SearchQueryResult.Success -> AdvancedSearchRequestResult.Success(result.query)
    is SearchQueryResult.Invalid -> AdvancedSearchRequestResult.Error(result.error)
}
