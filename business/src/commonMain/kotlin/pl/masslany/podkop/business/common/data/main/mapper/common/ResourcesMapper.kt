package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.domain.models.common.Resources


fun ResourceResponseDto.toResources() = Resources(
    data = data.toResourceItemList(),
    pagination = pagination?.toPagination(),
)