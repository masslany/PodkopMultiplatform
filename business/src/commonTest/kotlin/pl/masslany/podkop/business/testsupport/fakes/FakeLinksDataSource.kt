package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.common.pagination.PageRequest

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.common.domain.models.common.VoteReason
import pl.masslany.podkop.business.links.data.api.LinksDataSource
import pl.masslany.podkop.business.links.data.network.models.LinkDraftCheckResponseDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftResponseDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftsResponseDto
import pl.masslany.podkop.business.links.data.network.models.LinkUpvotesResponseDto
import pl.masslany.podkop.business.links.domain.models.request.PublishLinkDraft
import pl.masslany.podkop.business.links.domain.models.request.UpdateLinkDraft

class FakeLinksDataSource : LinksDataSource {
    data class GetLinksCall(
        val page: PageRequest,
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

    data class RelatedLinkVoteCall(
        val linkId: Int,
        val relatedId: Int,
    )

    data class LinkVoteDownCall(
        val linkId: Int,
        val reason: VoteReason,
    )

    data class CreateLinkCommentCall(
        val linkId: Int,
        val content: String,
        val adult: Boolean,
        val photoKey: String?,
    )

    data class CreateLinkDraftCall(
        val url: String,
    )

    data class PublishLinkDraftCall(
        val key: String,
        val request: PublishLinkDraft,
    )

    data class GetLinkDraftCall(
        val key: String,
    )

    data class UpdateLinkDraftCall(
        val key: String,
        val request: UpdateLinkDraft,
    )

    data class DeleteLinkDraftCall(
        val key: String,
    )

    data class CreateLinkCommentReplyCall(
        val linkId: Int,
        val commentId: Int,
        val content: String,
        val adult: Boolean,
        val photoKey: String?,
    )

    data class UpdateLinkCommentCall(
        val linkId: Int,
        val commentId: Int,
        val content: String,
        val adult: Boolean,
        val photoKey: String?,
    )

    data class GetLinkUpvotesCall(
        val linkId: Int,
        val type: String,
        val page: Int?,
    )

    var getLinksResult: Result<ResourceResponseDto> = unstubbedResult("LinksDataSource.getLinks")
    var getLinkResult: Result<SingleResourceResponseDto> = unstubbedResult("LinksDataSource.getLink")
    var getCommentsResult: Result<ResourceResponseDto> = unstubbedResult("LinksDataSource.getComments")
    var getSubCommentsResult: Result<ResourceResponseDto> = unstubbedResult("LinksDataSource.getSubComments")
    var getRelatedLinksResult: Result<ResourceResponseDto> = unstubbedResult("LinksDataSource.getRelatedLinks")
    var createLinkDraftResult: Result<LinkDraftCheckResponseDto> = unstubbedResult("LinksDataSource.createLinkDraft")
    var getLinkDraftsResult: Result<LinkDraftsResponseDto> = unstubbedResult("LinksDataSource.getLinkDrafts")
    var getLinkDraftResult: Result<LinkDraftResponseDto> = unstubbedResult("LinksDataSource.getLinkDraft")
    var updateLinkDraftResult: Result<Unit> = unstubbedResult("LinksDataSource.updateLinkDraft")
    var deleteLinkDraftResult: Result<Unit> = unstubbedResult("LinksDataSource.deleteLinkDraft")
    var publishLinkDraftResult: Result<Unit> = unstubbedResult("LinksDataSource.publishLinkDraft")
    var createLinkCommentResult: Result<SingleResourceResponseDto> =
        unstubbedResult("LinksDataSource.createLinkComment")
    var createLinkCommentReplyResult: Result<SingleResourceResponseDto> =
        unstubbedResult("LinksDataSource.createLinkCommentReply")
    var updateLinkCommentResult: Result<SingleResourceResponseDto> =
        unstubbedResult("LinksDataSource.updateLinkComment")
    var getLinkUpvotesResult: Result<LinkUpvotesResponseDto> = unstubbedResult("LinksDataSource.getLinkUpvotes")
    var voteOnLinkResult: Result<Unit> = unstubbedResult("LinksDataSource.voteOnLink")
    var voteDownOnLinkResult: Result<Unit> = unstubbedResult("LinksDataSource.voteDownOnLink")
    var removeVoteOnLinkResult: Result<Unit> = unstubbedResult("LinksDataSource.removeVoteOnLink")
    var voteUpOnRelatedLinkResult: Result<Unit> = unstubbedResult("LinksDataSource.voteUpOnRelatedLink")
    var voteDownOnRelatedLinkResult: Result<Unit> = unstubbedResult("LinksDataSource.voteDownOnRelatedLink")
    var removeVoteOnRelatedLinkResult: Result<Unit> = unstubbedResult("LinksDataSource.removeVoteOnRelatedLink")
    var voteOnLinkCommentResult: Result<Unit> = unstubbedResult("LinksDataSource.voteOnLinkComment")
    var voteDownOnLinkCommentResult: Result<Unit> = unstubbedResult("LinksDataSource.voteDownOnLinkComment")
    var removeVoteOnLinkCommentResult: Result<Unit> = unstubbedResult("LinksDataSource.removeVoteOnLinkComment")

    val getLinksCalls = mutableListOf<GetLinksCall>()
    val getLinkCalls = mutableListOf<Int>()
    val getCommentsCalls = mutableListOf<GetCommentsCall>()
    val getSubCommentsCalls = mutableListOf<GetSubCommentsCall>()
    val getRelatedLinksCalls = mutableListOf<Int>()
    val createLinkDraftCalls = mutableListOf<CreateLinkDraftCall>()
    val getLinkDraftsCalls = mutableListOf<Unit>()
    val getLinkDraftCalls = mutableListOf<GetLinkDraftCall>()
    val updateLinkDraftCalls = mutableListOf<UpdateLinkDraftCall>()
    val deleteLinkDraftCalls = mutableListOf<DeleteLinkDraftCall>()
    val publishLinkDraftCalls = mutableListOf<PublishLinkDraftCall>()
    val createLinkCommentCalls = mutableListOf<CreateLinkCommentCall>()
    val createLinkCommentReplyCalls = mutableListOf<CreateLinkCommentReplyCall>()
    val updateLinkCommentCalls = mutableListOf<UpdateLinkCommentCall>()
    val getLinkUpvotesCalls = mutableListOf<GetLinkUpvotesCall>()
    val voteOnLinkCalls = mutableListOf<Int>()
    val voteDownOnLinkCalls = mutableListOf<LinkVoteDownCall>()
    val removeVoteOnLinkCalls = mutableListOf<Int>()
    val voteUpOnRelatedLinkCalls = mutableListOf<RelatedLinkVoteCall>()
    val voteDownOnRelatedLinkCalls = mutableListOf<RelatedLinkVoteCall>()
    val removeVoteOnRelatedLinkCalls = mutableListOf<RelatedLinkVoteCall>()
    val voteOnLinkCommentCalls = mutableListOf<LinkCommentVoteCall>()
    val voteDownOnLinkCommentCalls = mutableListOf<LinkCommentVoteCall>()
    val removeVoteOnLinkCommentCalls = mutableListOf<LinkCommentVoteCall>()

    override suspend fun getLinks(
        page: PageRequest,
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

    override suspend fun createLinkDraft(url: String): Result<LinkDraftCheckResponseDto> {
        createLinkDraftCalls += CreateLinkDraftCall(url = url)
        return createLinkDraftResult
    }

    override suspend fun getLinkDrafts(): Result<LinkDraftsResponseDto> {
        getLinkDraftsCalls += Unit
        return getLinkDraftsResult
    }

    override suspend fun getLinkDraft(key: String): Result<LinkDraftResponseDto> {
        getLinkDraftCalls += GetLinkDraftCall(key = key)
        return getLinkDraftResult
    }

    override suspend fun updateLinkDraft(key: String, request: UpdateLinkDraft): Result<Unit> {
        updateLinkDraftCalls += UpdateLinkDraftCall(key = key, request = request)
        return updateLinkDraftResult
    }

    override suspend fun deleteLinkDraft(key: String): Result<Unit> {
        deleteLinkDraftCalls += DeleteLinkDraftCall(key = key)
        return deleteLinkDraftResult
    }

    override suspend fun publishLinkDraft(key: String, request: PublishLinkDraft): Result<Unit> {
        publishLinkDraftCalls += PublishLinkDraftCall(key = key, request = request)
        return publishLinkDraftResult
    }

    override suspend fun createLinkComment(
        linkId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        createLinkCommentCalls += CreateLinkCommentCall(linkId, content, adult, photoKey)
        return createLinkCommentResult
    }

    override suspend fun createLinkCommentReply(
        linkId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        createLinkCommentReplyCalls += CreateLinkCommentReplyCall(linkId, commentId, content, adult, photoKey)
        return createLinkCommentReplyResult
    }

    override suspend fun updateLinkComment(
        linkId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        updateLinkCommentCalls += UpdateLinkCommentCall(linkId, commentId, content, adult, photoKey)
        return updateLinkCommentResult
    }

    override suspend fun getLinkUpvotes(linkId: Int, type: String, page: Int?): Result<LinkUpvotesResponseDto> {
        getLinkUpvotesCalls += GetLinkUpvotesCall(linkId, type, page)
        return getLinkUpvotesResult
    }

    override suspend fun voteOnLink(linkId: Int): Result<Unit> {
        voteOnLinkCalls += linkId
        return voteOnLinkResult
    }

    override suspend fun voteDownOnLink(linkId: Int, reason: VoteReason): Result<Unit> {
        voteDownOnLinkCalls += LinkVoteDownCall(linkId, reason)
        return voteDownOnLinkResult
    }

    override suspend fun removeVoteOnLink(linkId: Int): Result<Unit> {
        removeVoteOnLinkCalls += linkId
        return removeVoteOnLinkResult
    }

    override suspend fun voteUpOnRelatedLink(linkId: Int, relatedId: Int): Result<Unit> {
        voteUpOnRelatedLinkCalls += RelatedLinkVoteCall(linkId, relatedId)
        return voteUpOnRelatedLinkResult
    }

    override suspend fun voteDownOnRelatedLink(linkId: Int, relatedId: Int): Result<Unit> {
        voteDownOnRelatedLinkCalls += RelatedLinkVoteCall(linkId, relatedId)
        return voteDownOnRelatedLinkResult
    }

    override suspend fun removeVoteOnRelatedLink(linkId: Int, relatedId: Int): Result<Unit> {
        removeVoteOnRelatedLinkCalls += RelatedLinkVoteCall(linkId, relatedId)
        return removeVoteOnRelatedLinkResult
    }

    override suspend fun voteOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        voteOnLinkCommentCalls += LinkCommentVoteCall(linkId, commentId)
        return voteOnLinkCommentResult
    }

    override suspend fun voteDownOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        voteDownOnLinkCommentCalls += LinkCommentVoteCall(linkId, commentId)
        return voteDownOnLinkCommentResult
    }

    override suspend fun removeVoteOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        removeVoteOnLinkCommentCalls += LinkCommentVoteCall(linkId, commentId)
        return removeVoteOnLinkCommentResult
    }
}
