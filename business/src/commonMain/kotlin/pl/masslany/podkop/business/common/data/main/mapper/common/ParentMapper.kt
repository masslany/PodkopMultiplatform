package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.network.models.comments.ParentDto
import pl.masslany.podkop.business.common.domain.models.common.Parent

fun ParentDto.toParent(): Parent {
    return Parent(
        id = this.id
    )
}