package pl.masslany.podkop.widget

import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem

/** The day's hits as the widget last fetched them; thumbnails live next to it as files named by link id. */
@Serializable
data class HitsWidgetSnapshot(
    val updatedAtMillis: Long,
    val items: List<HitsWidgetItem>,
) {
    companion object {
        const val MAX_ITEMS = 10
    }
}

@Serializable
data class HitsWidgetItem(
    val id: Int,
    val title: String,
    val votes: Int,
    val isHot: Boolean = false,
    val comments: Int,
    val source: String? = null,
    val thumbnailUrl: String? = null,
)

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

internal fun List<ResourceItem>.toHitsWidgetItems(
    limit: Int = HitsWidgetSnapshot.MAX_ITEMS,
): List<HitsWidgetItem> = asSequence()
    .filter { it.resource == Resource.Link && it.deleted == Deleted.None && it.title.isNotBlank() }
    .take(limit)
    .map { item ->
        HitsWidgetItem(
            id = item.id,
            title = item.title,
            votes = item.votes?.up ?: 0,
            isHot = item.hot,
            comments = item.comments?.count ?: 0,
            source = item.source?.label?.takeIf(String::isNotBlank),
            // The app covers adult images until they are tapped, so the widget leaves them out.
            thumbnailUrl = item.media?.photo?.url?.takeIf { it.isNotBlank() && !item.adult },
        )
    }
    .toList()
