package pl.masslany.podkop.business.links.data.network.main

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.links.data.api.LinksDataSource
import pl.masslany.podkop.business.links.data.network.api.LinksApi
import pl.masslany.podkop.business.links.data.network.models.LinkUpvotesResponseDto

class LinksDataSourceImpl(
    private val linksApi: LinksApi,
) : LinksDataSource {
    override suspend fun getLinks(
        page: Any?,
        limit: Int?,
        sort: String,
        type: String,
        category: String?,
        bucket: String?,
    ): Result<ResourceResponseDto> {
        return linksApi.getLinks(page, limit, sort, type, category, bucket)
    }

    override suspend fun getLink(id: Int): Result<SingleResourceResponseDto> {
        return linksApi.getLink(id)
    }

    override suspend fun getComments(
        id: Int,
        page: Int?,
        limit: Int?,
        sort: String?,
        ama: Boolean?,
    ): Result<ResourceResponseDto> {
        return linksApi.getComments(id, page, limit, sort, ama)
    }

    override suspend fun getSubComments(
        linkId: Int,
        commentId: Int,
        page: Int?,
    ): Result<ResourceResponseDto> {
        return linksApi.getSubComments(linkId, commentId, page)
    }

    override suspend fun getRelatedLinks(linkId: Int): Result<ResourceResponseDto> {
        return linksApi.getRelatedLinks(linkId)
    }

    override suspend fun createLinkComment(
        linkId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        return linksApi.createLinkComment(
            linkId = linkId,
            content = content,
            adult = adult,
            photoKey = photoKey,
        )
    }

    override suspend fun createLinkCommentReply(
        linkId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        return linksApi.createLinkCommentReply(
            linkId = linkId,
            commentId = commentId,
            content = content,
            adult = adult,
            photoKey = photoKey,
        )
    }

    override suspend fun updateLinkComment(
        linkId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        return linksApi.updateLinkComment(
            linkId = linkId,
            commentId = commentId,
            content = content,
            adult = adult,
            photoKey = photoKey,
        )
    }

    override suspend fun getLinkUpvotes(linkId: Int, type: String, page: Int?): Result<LinkUpvotesResponseDto> {
        return linksApi.getLinkUpvotes(linkId, type, page)
    }

    override suspend fun voteOnLink(linkId: Int): Result<Unit> {
        return linksApi.voteOnLink(linkId)
    }

    override suspend fun removeVoteOnLink(linkId: Int): Result<Unit> {
        return linksApi.removeVoteOnLink(linkId)
    }

    override suspend fun voteOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        return linksApi.voteOnLinkComment(linkId, commentId)
    }

    override suspend fun voteDownOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        return linksApi.voteDownOnLinkComment(linkId, commentId)
    }

    override suspend fun removeVoteOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        return linksApi.removeVoteOnLinkComment(linkId, commentId)
    }
}
