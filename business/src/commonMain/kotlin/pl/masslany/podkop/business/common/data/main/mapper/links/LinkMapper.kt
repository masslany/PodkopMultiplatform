package pl.masslany.podkop.business.common.data.main.mapper.links

import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.common.domain.models.links.Link
import pl.masslany.podkop.business.common.data.main.mapper.common.toResourceItemList

fun SingleResourceResponseDto.toLink(): Link {
    return Link(
        data = listOf(this.data).toResourceItemList().first(),
    )
}
