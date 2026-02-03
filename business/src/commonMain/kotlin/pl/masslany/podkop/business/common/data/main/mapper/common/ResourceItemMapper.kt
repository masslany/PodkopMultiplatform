package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.main.mapper.toResource
import pl.masslany.podkop.business.common.data.network.models.common.ResourceItemDto
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem

fun List<ResourceItemDto>.toResourceItemList(): List<ResourceItem> {
    return this.map {
        ResourceItem(
            actions = it.actions?.toActions(),
            adult = it.adult ?: false,
            archive = it.archive ?: false,
            author = it.author?.toAuthor(),
            comments = it.comments?.toComments(),
            content = it.content.orEmpty(),
            createdAt = it.createdAt,
            deletable = it.deletable ?: false,
            deleted = it.deleted.toDeleted(),
            description = it.description.orEmpty(),
            editable = it.editable ?: false,
            hot = it.hot ?: false,
            id = it.id ?: -1,
            media = it.media?.toMedia(),
            name = it.name.orEmpty(),
            parent = it.parent?.toParent(),
            publishedAt = it.publishedAt,
            recommended = it.recommended ?: false,
            resource = it.resource.toResource(),
            slug = it.slug.orEmpty(),
            source = it.source?.toSource(),
            tags = it.tags ?: emptyList(),
            title = it.title.orEmpty(),
            voted = it.voted == 1,
            votes = it.votes?.toVotes(),
        )
    }
}
