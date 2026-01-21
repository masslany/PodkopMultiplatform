package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.network.models.common.SourceDto
import pl.masslany.podkop.business.common.domain.models.common.Source

fun SourceDto.toSource(): Source {
    return Source(
        label = this.label,
        // TODO: Domain model & mapper
        type = this.type.orEmpty(),
        typeId = this.typeId ?: -1,
        url = this.url,
    )
}
