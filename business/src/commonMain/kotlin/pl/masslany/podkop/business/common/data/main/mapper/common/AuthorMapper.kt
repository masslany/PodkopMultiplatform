package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.network.models.common.AuthorDto
import pl.masslany.podkop.business.common.domain.models.common.Author
import pl.masslany.podkop.business.common.data.main.mapper.toGender
import pl.masslany.podkop.business.common.data.main.mapper.toNameColor


fun AuthorDto.toAuthor(): Author {
    return Author(
        avatar = this.avatar,
        blacklist = this.blacklist,
        color = this.color.toNameColor(),
        company = this.company,
        follow = this.follow,
        gender = this.gender.toGender(),
        note = this.note,
        online = this.online,
        rank = this.rank.toRank(),
        status = this.status,
        username = this.username,
        verified = this.verified,
    )
}
