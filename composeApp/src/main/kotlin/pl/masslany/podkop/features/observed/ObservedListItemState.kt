package pl.masslany.podkop.features.observed

import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.observed.domain.models.ObservedResource
import pl.masslany.podkop.features.resources.models.ResourceItemState

data class ObservedListItemState(
    val key: String,
    val resource: ResourceItemState,
    val discussionBanner: ObservedDiscussionBannerState? = null,
)

data class ObservedDiscussionBannerState(val newContentCount: Int, val type: ObservedDiscussionBannerType)

enum class ObservedDiscussionBannerType {
    Entry,
    Link,
}

internal fun ObservedResource.toObservedListItemState(
    resourceState: ResourceItemState,
): ObservedListItemState = ObservedListItemState(
    key = item.resource.toObservedItemKey(item.id),
    resource = resourceState,
    discussionBanner = item.resource.toObservedDiscussionBannerState(newContentCount),
)

internal fun ResourceItemState.toObservedListItemState(): ObservedListItemState = ObservedListItemState(
    key = "${contentType.name}:$id",
    resource = this,
)

internal fun Resource.toObservedDiscussionBannerState(
    newContentCount: Int?,
): ObservedDiscussionBannerState? {
    val bannerType = when (this) {
        Resource.Entry -> ObservedDiscussionBannerType.Entry
        Resource.Link -> ObservedDiscussionBannerType.Link
        else -> null
    } ?: return null

    return newContentCount
        ?.takeIf { count -> count > 0 }
        ?.let { count ->
            ObservedDiscussionBannerState(
                newContentCount = count,
                type = bannerType,
            )
        }
}
