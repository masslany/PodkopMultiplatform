package pl.masslany.podkop.business.links.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.data.main.mapper.common.toMedia
import pl.masslany.podkop.business.common.data.main.mapper.common.toResourceItemList
import pl.masslany.podkop.business.common.data.main.mapper.common.toResources
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.data.main.mapper.links.toLink
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.common.domain.models.common.VoteReason
import pl.masslany.podkop.business.common.domain.models.links.Link
import pl.masslany.podkop.business.common.domain.models.common.Voters
import pl.masslany.podkop.business.links.data.main.mapper.toVoters
import pl.masslany.podkop.business.links.data.api.LinksDataSource
import pl.masslany.podkop.business.links.data.network.models.LinkDraftImageDto
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.business.links.domain.models.LinkDraftCheck
import pl.masslany.podkop.business.links.domain.models.LinkDraftDetails
import pl.masslany.podkop.business.links.domain.models.request.CommentsSortType
import pl.masslany.podkop.business.links.domain.models.request.LinksSortType
import pl.masslany.podkop.business.links.domain.models.request.LinksType
import pl.masslany.podkop.business.links.domain.models.request.PublishLinkDraft
import pl.masslany.podkop.business.links.domain.models.request.UpdateLinkDraft
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class LinksRepositoryImpl(
    private val linksDataSource: LinksDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : LinksRepository {
    override suspend fun getLinks(
        page: Any?,
        limit: Int?,
        linksSortType: LinksSortType,
        linksType: LinksType,
        category: String?,
        bucket: String?,
    ): Result<Resources> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.getLinks(
                page,
                limit,
                linksSortType.value,
                linksType.value,
                category,
                bucket,
            ).mapCatching {
                it.toResources()
            }
        }
    }

    override fun getLinksSortTypes(isUpcoming: Boolean): List<LinksSortType> {
        return if (isUpcoming) {
            listOf(
                LinksSortType.Newest,
                LinksSortType.Active,
                LinksSortType.Commented,
                LinksSortType.Digged,
            )
        } else {
            listOf(LinksSortType.Newest, LinksSortType.Active)
        }
    }

    override fun getCommentsSortTypes(): List<CommentsSortType> {
        return listOf(CommentsSortType.Best, CommentsSortType.Newest, CommentsSortType.Oldest)
    }

    override suspend fun getLink(id: Int): Result<Link> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.getLink(id).mapCatching {
                it.toLink()
            }
        }
    }

    override suspend fun getComments(
        id: Int,
        page: Int?,
        limit: Int?,
        commentSortType: CommentsSortType,
        ama: Boolean?,
    ): Result<Resources> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.getComments(id, page, limit, commentSortType.value, ama).mapCatching {
                it.toResources()
            }
        }
    }

    override suspend fun getSubComments(
        linkId: Int,
        commentId: Int,
        page: Int?,
    ): Result<Resources> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.getSubComments(linkId, commentId, page).mapCatching {
                it.toResources()
            }
        }
    }

    override suspend fun getRelatedLinks(linkId: Int): Result<Resources> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.getRelatedLinks(linkId).mapCatching {
                it.toResources(defaultResource = Resource.Link)
            }
        }
    }

    override suspend fun createLinkDraft(url: String): Result<LinkDraftCheck> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.createLinkDraft(url).mapCatching {
                LinkDraftCheck(
                    key = it.data.key,
                    similar = it.data.similar.toResourceItemList(),
                    duplicate = it.data.duplicate,
                )
            }
        }
    }

    override suspend fun getLinkDrafts(): Result<List<LinkDraftDetails>> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.getLinkDrafts().mapCatching { response ->
                response.data.map { it.toLinkDraftDetails() }
            }
        }
    }

    override suspend fun getLinkDraft(key: String): Result<LinkDraftDetails> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.getLinkDraft(key).mapCatching {
                it.data.toLinkDraftDetails()
            }
        }
    }

    override suspend fun updateLinkDraft(key: String, request: UpdateLinkDraft): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.updateLinkDraft(key = key, request = request)
        }
    }

    override suspend fun deleteLinkDraft(key: String): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.deleteLinkDraft(key)
        }
    }

    override suspend fun publishLinkDraft(key: String, request: PublishLinkDraft): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.publishLinkDraft(key = key, request = request)
        }
    }

    override suspend fun createLinkComment(
        linkId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.createLinkComment(
                linkId = linkId,
                content = content,
                adult = adult,
                photoKey = photoKey,
            ).mapCatching {
                listOf(it.data).toResourceItemList().first()
            }
        }
    }

    override suspend fun createLinkCommentReply(
        linkId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.createLinkCommentReply(
                linkId = linkId,
                commentId = commentId,
                content = content,
                adult = adult,
                photoKey = photoKey,
            ).mapCatching {
                listOf(it.data).toResourceItemList().first()
            }
        }
    }

    override suspend fun updateLinkComment(
        linkId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.updateLinkComment(
                linkId = linkId,
                commentId = commentId,
                content = content,
                adult = adult,
                photoKey = photoKey,
            ).mapCatching {
                listOf(it.data).toResourceItemList().first()
            }
        }
    }

    override suspend fun getLinkUpvotes(linkId: Int, type: String, page: Int?): Result<Voters> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.getLinkUpvotes(linkId, type, page).mapCatching {
                it.toVoters()
            }
        }
    }

    override suspend fun voteOnLink(linkId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.voteOnLink(linkId)
        }
    }

    override suspend fun voteDownOnLink(linkId: Int, reason: VoteReason): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.voteDownOnLink(linkId, reason)
        }
    }

    override suspend fun removeVoteOnLink(linkId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.removeVoteOnLink(linkId)
        }
    }

    override suspend fun voteUpOnRelatedLink(linkId: Int, relatedId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.voteUpOnRelatedLink(linkId, relatedId)
        }
    }

    override suspend fun voteDownOnRelatedLink(linkId: Int, relatedId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.voteDownOnRelatedLink(linkId, relatedId)
        }
    }

    override suspend fun removeVoteOnRelatedLink(linkId: Int, relatedId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.removeVoteOnRelatedLink(linkId, relatedId)
        }
    }

    override suspend fun voteOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.voteOnLinkComment(linkId, commentId)
        }
    }

    override suspend fun voteDownOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.voteDownOnLinkComment(linkId, commentId)
        }
    }

    override suspend fun removeVoteOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.removeVoteOnLinkComment(linkId, commentId)
        }
    }
}

private fun pl.masslany.podkop.business.links.data.network.models.LinkDraftDto.toLinkDraftDetails(): LinkDraftDetails {
    val media = media?.toMedia()
    return LinkDraftDetails(
        key = key,
        url = url,
        title = title,
        description = description,
        tags = tags,
        adult = adult,
        photoKey = media?.photo?.key,
        photoUrl = media?.photo?.url,
        suggestedImages = images.map(LinkDraftImageDto::url),
        selectedImageIndex = images.indexOfFirst(LinkDraftImageDto::selected)
            .takeIf { it >= 0 }
            ?: images.indices.firstOrNull(),
    )
}
