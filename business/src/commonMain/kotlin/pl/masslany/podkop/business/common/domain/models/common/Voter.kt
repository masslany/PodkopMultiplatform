package pl.masslany.podkop.business.common.domain.models.common

data class Voter(
    val username: String,
    val avatar: String,
    val gender: Gender,
    val color: NameColor,
    val online: Boolean,
    val company: Boolean,
    val verified: Boolean,
    val status: String,
    val reason: VoteReason? = null,
)
