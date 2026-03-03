package pl.masslany.podkop.business.links.data.main.mapper

import pl.masslany.podkop.business.common.data.main.mapper.common.toPagination
import pl.masslany.podkop.business.common.data.main.mapper.toGender
import pl.masslany.podkop.business.common.data.main.mapper.toNameColor
import pl.masslany.podkop.business.common.domain.models.common.VoteReason
import pl.masslany.podkop.business.common.domain.models.common.Voter
import pl.masslany.podkop.business.common.domain.models.common.Voters
import pl.masslany.podkop.business.links.data.network.models.LinkUpvoteItemDto
import pl.masslany.podkop.business.links.data.network.models.LinkUpvotesResponseDto

fun LinkUpvotesResponseDto.toVoters(): Voters {
    return Voters(
        data = data.map { it.toVoter() },
        pagination = pagination?.toPagination(),
    )
}

private fun LinkUpvoteItemDto.toVoter(): Voter {
    return Voter(
        username = user.username,
        avatar = user.avatar,
        gender = user.gender.toGender(),
        color = user.color.toNameColor(),
        online = user.online,
        company = user.company,
        verified = user.verified,
        status = user.status,
        reason = reason.toVoteReason(),
    )
}

private fun String.toVoteReason(): VoteReason? = when (this) {
    "duplicate" -> VoteReason.Duplicate
    "spam" -> VoteReason.Spam
    "fake" -> VoteReason.Fake
    "wrong" -> VoteReason.Wrong
    "invalid" -> VoteReason.Invalid
    else -> null
}
