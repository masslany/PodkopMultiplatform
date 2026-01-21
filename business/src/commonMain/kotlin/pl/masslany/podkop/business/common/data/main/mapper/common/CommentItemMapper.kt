package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.domain.models.common.Comment
import pl.masslany.podkop.business.common.data.main.mapper.toResource
import pl.masslany.podkop.business.common.data.network.models.comments.CommentItemDto


fun List<CommentItemDto>.toCommentList(): List<Comment> {
    return this.map {
        Comment(
            actions = it.actions.toActions(),
            adult = it.adult,
            archive = it.archive,
            author = it.author.toAuthor(),
            comments = null,
            createdAt = it.createdAt,
            deletable = it.deletable,
            editable = it.editable,
            id = it.id,
            media = it.media.toMedia(),
            resource = it.resource.toResource(),
            slug = it.slug,
            tags = it.tags,
            voted = it.voted,
            votes = it.votes.toVotes(),
            content = it.content,
            blacklist = it.blacklist,
            deleted = it.deleted.toDeleted(),
            device = it.device,
            favourite = it.favourite,
            parentId = it.parentId ?: -1,
        )
    }
}
