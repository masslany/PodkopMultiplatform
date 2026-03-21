package pl.masslany.podkop.features.search

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.business.search.domain.models.request.SearchSort
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class AdvancedSearchScreenState(
    val query: String,
    val sort: SearchSort,
    val minimumVotes: Int?,
    val datePreset: AdvancedSearchDatePreset,
    val customDateFrom: String,
    val customDateTo: String,
    val tags: String,
    val users: String,
    val domains: String,
    val category: String,
    val results: ImmutableList<ResourceItemState>,
    val hasSearched: Boolean,
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val isError: Boolean,
    val isPaginating: Boolean,
    val totalResults: Int?,
    val validationError: AdvancedSearchValidationError?,
) {
    companion object {
        val initial = AdvancedSearchScreenState(
            query = "",
            sort = SearchSort.Score,
            minimumVotes = null,
            datePreset = AdvancedSearchDatePreset.AnyTime,
            customDateFrom = "",
            customDateTo = "",
            tags = "",
            users = "",
            domains = "",
            category = "",
            results = persistentListOf(),
            hasSearched = false,
            isLoading = false,
            isRefreshing = false,
            isError = false,
            isPaginating = false,
            totalResults = null,
            validationError = null,
        )
    }

    val isSearchButtonEnabled: Boolean
        get() = query.trim().isNotEmpty() && !isLoading && !isRefreshing

    val isCustomDateRangeVisible: Boolean
        get() = datePreset == AdvancedSearchDatePreset.Custom
}

enum class AdvancedSearchDatePreset {
    AnyTime,
    Last24Hours,
    Last3Days,
    Last7Days,
    Last30Days,
    LastYear,
    Custom,
}

enum class AdvancedSearchValidationError {
    QueryRequired,
    InvalidCustomDateFormat,
    InvalidCustomDateRange,
}
