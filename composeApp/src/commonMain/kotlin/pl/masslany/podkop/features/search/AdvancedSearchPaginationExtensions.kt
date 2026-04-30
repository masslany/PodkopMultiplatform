package pl.masslany.podkop.features.search

import pl.masslany.podkop.business.common.domain.models.common.Resources

internal fun Resources.withSearchFallbackPagination(
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
