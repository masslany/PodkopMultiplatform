package pl.masslany.podkop.business.links.data.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.common.domain.models.common.VoteReason
import pl.masslany.podkop.business.links.data.network.models.LinkDraftCheckResponseDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftResponseDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftsResponseDto
import pl.masslany.podkop.business.links.data.network.models.LinkUpvotesResponseDto
import pl.masslany.podkop.business.links.domain.models.request.PublishLinkDraft
import pl.masslany.podkop.business.links.domain.models.request.UpdateLinkDraft

interface LinksDataSource {
    @Suppress("LongParameterList")
    suspend fun getLinks(
        page: Any?,
        limit: Int?,
        sort: String,
        type: String,
        category: String?,
        bucket: String?,
    ): Result<ResourceResponseDto>

    suspend fun getLink(id: Int): Result<SingleResourceResponseDto>

    @Suppress("LongParameterList")
    suspend fun getComments(
        id: Int,
        page: Int?,
        limit: Int?,
        sort: String?,
        ama: Boolean?,
    ): Result<ResourceResponseDto>

    suspend fun getSubComments(
        linkId: Int,
        commentId: Int,
        page: Int?,
    ): Result<ResourceResponseDto>

    suspend fun getRelatedLinks(linkId: Int): Result<ResourceResponseDto>

    suspend fun createLinkDraft(url: String): Result<LinkDraftCheckResponseDto>

    suspend fun getLinkDrafts(): Result<LinkDraftsResponseDto>

    suspend fun getLinkDraft(key: String): Result<LinkDraftResponseDto>

    suspend fun updateLinkDraft(key: String, request: UpdateLinkDraft): Result<Unit>

    suspend fun deleteLinkDraft(key: String): Result<Unit>

    suspend fun publishLinkDraft(key: String, request: PublishLinkDraft): Result<Unit>

    suspend fun createLinkComment(
        linkId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto>

    suspend fun createLinkCommentReply(
        linkId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto>

    suspend fun updateLinkComment(
        linkId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto>

    suspend fun getLinkUpvotes(linkId: Int, type: String, page: Int?): Result<LinkUpvotesResponseDto>

    suspend fun voteOnLink(linkId: Int): Result<Unit>

    suspend fun voteDownOnLink(linkId: Int, reason: VoteReason): Result<Unit>

    suspend fun removeVoteOnLink(linkId: Int): Result<Unit>

    suspend fun voteUpOnRelatedLink(linkId: Int, relatedId: Int): Result<Unit>

    suspend fun voteDownOnRelatedLink(linkId: Int, relatedId: Int): Result<Unit>

    suspend fun removeVoteOnRelatedLink(linkId: Int, relatedId: Int): Result<Unit>

    suspend fun voteOnLinkComment(linkId: Int, commentId: Int): Result<Unit>

    suspend fun voteDownOnLinkComment(linkId: Int, commentId: Int): Result<Unit>

    suspend fun removeVoteOnLinkComment(linkId: Int, commentId: Int): Result<Unit>
}
