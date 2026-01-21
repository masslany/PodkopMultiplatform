package pl.masslany.podkop.business.common.domain.models.common

data class Author(
    val avatar: String,
    val blacklist: Boolean,
    val color: NameColor,
    val company: Boolean,
    val follow: Boolean,
    val gender: Gender,
    val note: Boolean,
    val online: Boolean,
    val rank: Rank,
    val status: String,
    val username: String,
    val verified: Boolean,
)
