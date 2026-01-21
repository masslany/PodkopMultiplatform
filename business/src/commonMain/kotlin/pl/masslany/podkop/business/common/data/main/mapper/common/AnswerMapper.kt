package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.domain.models.common.Answer
import pl.masslany.podkop.business.common.data.network.models.entries.AnswerDto


fun AnswerDto.toAnswer(): Answer {
    return Answer(
        count = this.count,
        id = this.id,
        text = this.text,
        voted = this.voted,
    )
}
