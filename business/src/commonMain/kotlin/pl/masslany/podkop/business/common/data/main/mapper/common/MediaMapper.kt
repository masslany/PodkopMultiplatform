package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.network.models.common.MediaDto
import pl.masslany.podkop.business.common.domain.models.common.Media

fun MediaDto.toMedia(): Media {
    return Media(
        embed = this.embed?.toEmbed(),
        photo = this.photo?.toPhoto(),
        survey = this.survey?.toSurvey(),
    )
}
