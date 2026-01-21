package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.network.models.common.EmbedDto
import pl.masslany.podkop.business.common.domain.models.common.Embed

fun EmbedDto.toEmbed(): Embed {
    return Embed(
        key = this.key,
        thumbnail = this.thumbnail.orEmpty(),
        url = this.url,
        type = this.type,
    )
}
