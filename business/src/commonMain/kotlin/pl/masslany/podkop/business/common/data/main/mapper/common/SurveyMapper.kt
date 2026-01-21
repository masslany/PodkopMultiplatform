package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.network.models.entries.SurveyDto
import pl.masslany.podkop.business.common.domain.models.common.Survey

fun SurveyDto.toSurvey(): Survey {
    return Survey(
        actions = this.actions.toActions(),
        answers = this.answers.map { it.toAnswer() },
        count = this.count,
        deletable = this.deletable,
        editable = this.editable,
        key = this.key,
        question = this.question,
        voted = this.voted,
    )
}
