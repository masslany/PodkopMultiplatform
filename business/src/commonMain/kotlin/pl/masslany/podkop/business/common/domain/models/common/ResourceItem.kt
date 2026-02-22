package pl.masslany.podkop.business.common.domain.models.common

import kotlinx.datetime.LocalDateTime

data class ResourceItem(
    val actions: Actions?,
    val adult: Boolean,
    val archive: Boolean,
    val author: Author?,
    val comments: Comments?,
    val content: String,
    val createdAt: LocalDateTime?,
    val deleted: Deleted,
    val deletable: Boolean,
    val description: String,
    val editable: Boolean,
    val hot: Boolean,
    val id: Int,
    val media: Media?,
    val name: String,
    val parent: Parent?,
    val parentId: Int? = null,
    val publishedAt: LocalDateTime?,
    val recommended: Boolean,
    val resource: Resource,
    val slug: String,
    val source: Source?,
    val tags: List<String>,
    val title: String,
    val voted: Voted,
    val votes: Votes?,
)
