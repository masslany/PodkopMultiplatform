package pl.masslany.podkop.business.profile.domain.models

data class Summary(
    val actions: Int,
    val entries: Int,
    val links: Int,
    val followers: Int,
    val followingTags: Int,
    val followingUsers: Int,
)
