package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.links.data.api.LinksDataSource

class FakeLinksDataSource : LinksDataSource {
    data class GetLinksCall(
        val page: Any?,
        val limit: Int?,
        val sort: String,
        val type: String,
        val category: String?,
        val bucket: String?,
    )

    data class GetCommentsCall(
        val id: Int,
        val page: Int?,
        val limit: Int?,
        val sort: String?,
        val ama: Boolean?,
    )

    data class GetSubCommentsCall(
        val linkId: Int,
        val commentId: Int,
        val page: Int?,
    )

    data class LinkCommentVoteCall(
        val linkId: Int,
        val commentId: Int,
    )

    var getLinksResult: Result<ResourceResponseDto> = unstubbedResult("LinksDataSource.getLinks")
    var getLinkResult: Result<SingleResourceResponseDto> = unstubbedResult("LinksDataSource.getLink")
    var getCommentsResult: Result<ResourceResponseDto> = unstubbedResult("LinksDataSource.getComments")
    var getSubCommentsResult: Result<ResourceResponseDto> = unstubbedResult("LinksDataSource.getSubComments")
    var getRelatedLinksResult: Result<ResourceResponseDto> = unstubbedResult("LinksDataSource.getRelatedLinks")
    var voteOnLinkResult: Result<Unit> = unstubbedResult("LinksDataSource.voteOnLink")
    var removeVoteOnLinkResult: Result<Unit> = unstubbedResult("LinksDataSource.removeVoteOnLink")
    var voteOnLinkCommentResult: Result<Unit> = unstubbedResult("LinksDataSource.voteOnLinkComment")
    var removeVoteOnLinkCommentResult: Result<Unit> = unstubbedResult("LinksDataSource.removeVoteOnLinkComment")

    val getLinksCalls = mutableListOf<GetLinksCall>()
    val getLinkCalls = mutableListOf<Int>()
    val getCommentsCalls = mutableListOf<GetCommentsCall>()
    val getSubCommentsCalls = mutableListOf<GetSubCommentsCall>()
    val getRelatedLinksCalls = mutableListOf<Int>()
    val voteOnLinkCalls = mutableListOf<Int>()
    val removeVoteOnLinkCalls = mutableListOf<Int>()
    val voteOnLinkCommentCalls = mutableListOf<LinkCommentVoteCall>()
    val removeVoteOnLinkCommentCalls = mutableListOf<LinkCommentVoteCall>()

    override suspend fun getLinks(
        page: Any?,
        limit: Int?,
        sort: String,
        type: String,
        category: String?,
        bucket: String?,
    ): Result<ResourceResponseDto> {
        getLinksCalls += GetLinksCall(page, limit, sort, type, category, bucket)
        return getLinksResult
    }

    override suspend fun getLink(id: Int): Result<SingleResourceResponseDto> {
        getLinkCalls += id
        return getLinkResult
    }

    override suspend fun getComments(
        id: Int,
        page: Int?,
        limit: Int?,
        sort: String?,
        ama: Boolean?,
    ): Result<ResourceResponseDto> {
        getCommentsCalls += GetCommentsCall(id, page, limit, sort, ama)
        return getCommentsResult
    }

    override suspend fun getSubComments(
        linkId: Int,
        commentId: Int,
        page: Int?,
    ): Result<ResourceResponseDto> {
        getSubCommentsCalls += GetSubCommentsCall(linkId, commentId, page)
        return getSubCommentsResult
    }

    override suspend fun getRelatedLinks(linkId: Int): Result<ResourceResponseDto> {
        getRelatedLinksCalls += linkId
        return getRelatedLinksResult
    }

    override suspend fun voteOnLink(linkId: Int): Result<Unit> {
        voteOnLinkCalls += linkId
        return voteOnLinkResult
    }

    override suspend fun removeVoteOnLink(linkId: Int): Result<Unit> {
        removeVoteOnLinkCalls += linkId
        return removeVoteOnLinkResult
    }

    override suspend fun voteOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        voteOnLinkCommentCalls += LinkCommentVoteCall(linkId, commentId)
        return voteOnLinkCommentResult
    }

    override suspend fun removeVoteOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        removeVoteOnLinkCommentCalls += LinkCommentVoteCall(linkId, commentId)
        return removeVoteOnLinkCommentResult
    }
}
