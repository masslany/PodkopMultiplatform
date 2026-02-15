package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SummaryDto(
    @SerialName("actions")
    val actions: Int,
    @SerialName("entries")
    val entries: Int,
    @SerialName("entries_details")
    val entriesDetails: EntriesDetailsDto,
    @SerialName("followers")
    val followers: Int,
    @SerialName("following_tags")
    val followingTags: Int,
    @SerialName("following_users")
    val followingUsers: Int,
    @SerialName("links")
    val links: Int,
    @SerialName("links_details")
    val linksDetails: LinksDetailsDto,
)
