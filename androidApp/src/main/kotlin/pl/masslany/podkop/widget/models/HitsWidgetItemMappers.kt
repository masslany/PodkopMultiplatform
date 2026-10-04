package pl.masslany.podkop.widget.models

import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem

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
