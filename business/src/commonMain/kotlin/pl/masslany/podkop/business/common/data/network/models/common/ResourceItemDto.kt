package pl.masslany.podkop.business.common.data.network.models.common

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.data.network.models.DateAsStringSerializer
import pl.masslany.podkop.business.common.data.network.models.FlexibleDeletedSerializer
import pl.masslany.podkop.business.common.data.network.models.comments.ParentDto

@Serializable
data class ResourceItemDto(
    @SerialName("actions")
    val actions: ActionsDto? = null,
    @SerialName("adult")
    val adult: Boolean? = null,
    @SerialName("archive")
    val archive: Boolean? = null,
    @SerialName("author")
    val author: AuthorDto? = null,
    @SerialName("blacklist")
    val blacklist: Boolean? = null,
    @SerialName("comments")
    val comments: CommentsDto? = null,
    @SerialName("content")
    val content: String? = null,
    @SerialName("created_at")
    @Serializable(with = DateAsStringSerializer::class)
    val createdAt: LocalDateTime? = null,
    @SerialName("deletable")
    val deletable: Boolean? = null,
    @SerialName("deleted")
    @Serializable(with = FlexibleDeletedSerializer::class)
    val deleted: String? = null,
    @SerialName("device")
    val device: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("editable")
    val editable: Boolean? = null,
    @SerialName("favourite")
    val favourite: Boolean? = null,
    @SerialName("hot")
    val hot: Boolean? = null,
    @SerialName("id")
    val id: Int? = null,
    @SerialName("media")
    val media: MediaDto? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("parent")
    val parent: ParentDto? = null,
    @SerialName("parent_id")
    val parentId: Int? = null,
    @SerialName("published_at")
    @Serializable(with = DateAsStringSerializer::class)
    val publishedAt: LocalDateTime? = null,
    @SerialName("recommended")
    val recommended: Boolean? = null,
    @SerialName("resource")
    val resource: String? = null,
    @SerialName("slug")
    val slug: String? = null,
    @SerialName("source")
    val source: SourceDto? = null,
    @SerialName("tags")
    val tags: List<String>? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("voted")
    val voted: Int? = null,
    @SerialName("votes")
    val votes: VotesDto? = null,
)
