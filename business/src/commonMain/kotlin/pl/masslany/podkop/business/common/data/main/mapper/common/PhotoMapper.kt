package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.network.models.common.PhotoDto
import pl.masslany.podkop.business.common.domain.models.common.Photo


fun PhotoDto.toPhoto(): Photo {
    return Photo(
        height = this.height,
        key = this.key,
        label = this.label,
        mimeType = this.mimeType,
        size = this.size,
        url = this.url,
        width = this.width,
    )
}
