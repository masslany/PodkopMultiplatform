package pl.masslany.podkop.widget.models

/** What the widget shows: the last snapshot, if any, and how the latest refresh went. */
data class HitsWidgetState(
    val snapshot: HitsWidgetSnapshot? = null,
    val isRefreshing: Boolean = false,
    val lastRefreshFailed: Boolean = false,
) {
    /** True when there is nothing to show yet, or the hourly refresh has clearly been missed. */
    fun needsRefresh(nowMillis: Long): Boolean =
        !isRefreshing && (snapshot == null || nowMillis - snapshot.updatedAtMillis >= STALE_AFTER_MILLIS)

    private companion object {
        const val STALE_AFTER_MILLIS = 2 * 60 * 60 * 1000L
    }
}
