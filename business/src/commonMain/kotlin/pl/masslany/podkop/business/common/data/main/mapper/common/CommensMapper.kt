package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.network.models.common.CommentsDto
import pl.masslany.podkop.business.common.domain.models.common.Comments


fun CommentsDto.toComments(): Comments {
    return Comments(
        count = this.count,
        hot = this.hot ?: false,
        items = this.items?.toCommentList() ?: emptyList(),
    )
}
