package pl.masslany.podkop.business.observed.data.main.mapper

import pl.masslany.podkop.business.common.data.main.mapper.common.toPagination
import pl.masslany.podkop.business.common.data.main.mapper.common.toResourceItemList
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.observed.data.network.models.ObservedResponseDto
import pl.masslany.podkop.business.observed.domain.models.ObservedResource
import pl.masslany.podkop.business.observed.domain.models.ObservedResources

fun ObservedResponseDto.toResources(
    defaultResource: Resource = Resource.Unknown,
): ObservedResources = ObservedResources(
    data = data.map { observedItem ->
        ObservedResource(
            item = listOf(observedItem.item)
                .toResourceItemList(defaultResource = defaultResource)
                .single(),
            newContentCount = observedItem.newContentCount,
        )
    },
    pagination = pagination?.toPagination(),
)
