package pl.masslany.podkop.business.common.domain.models.common

import kotlinx.datetime.LocalDateTime

data class Comment(
    val actions: Actions,
    val adult: Boolean,
    val archive: Boolean,
    val author: Author,
    val blacklist: Boolean,
    val comments: Comments?,
    val content: String,
    val createdAt: LocalDateTime?,
    val deletable: Boolean,
    val deleted: Deleted,
    val device: String,
    val editable: Boolean,
    val favourite: Boolean,
    val id: Int,
    val media: Media,
    val parentId: Int,
    val resource: Resource,
    val slug: String,
    val tags: List<String>,
    val voted: Int,
    val votes: Votes,
)
