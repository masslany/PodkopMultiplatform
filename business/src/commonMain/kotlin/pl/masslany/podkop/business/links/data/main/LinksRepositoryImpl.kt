package pl.masslany.podkop.business.links.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.data.main.mapper.common.toResources
import pl.masslany.podkop.business.common.data.main.mapper.links.toLink
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.common.domain.models.links.Link
import pl.masslany.podkop.business.links.data.api.LinksDataSource
import pl.masslany.podkop.business.links.domain.main.LinksRepository
import pl.masslany.podkop.business.links.domain.models.request.CommentsSortType
import pl.masslany.podkop.business.links.domain.models.request.LinksSortType
import pl.masslany.podkop.business.links.domain.models.request.LinksType
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
                it.toResources()
            }
        }
    }

    override suspend fun voteOnLink(linkId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.voteOnLink(linkId)
        }
    }

    override suspend fun removeVoteOnLink(linkId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.removeVoteOnLink(linkId)
        }
    }

    override suspend fun voteOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.voteOnLinkComment(linkId, commentId)
        }
    }

    override suspend fun removeVoteOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            linksDataSource.removeVoteOnLinkComment(linkId, commentId)
        }
    }
}
