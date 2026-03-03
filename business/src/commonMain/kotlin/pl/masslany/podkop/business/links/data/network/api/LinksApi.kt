package pl.masslany.podkop.business.links.data.network.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.links.data.network.models.LinkUpvotesResponseDto

interface LinksApi {
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

    suspend fun getLinkUpvotes(linkId: Int, type: String, page: Int?): Result<LinkUpvotesResponseDto>

    suspend fun voteOnLink(linkId: Int): Result<Unit>

    suspend fun removeVoteOnLink(linkId: Int): Result<Unit>

    suspend fun voteOnLinkComment(linkId: Int, commentId: Int): Result<Unit>

    suspend fun removeVoteOnLinkComment(linkId: Int, commentId: Int): Result<Unit>
}
