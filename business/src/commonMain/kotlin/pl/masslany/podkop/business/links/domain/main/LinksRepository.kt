package pl.masslany.podkop.business.links.domain.main

import pl.masslany.podkop.business.common.domain.models.links.Link
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.common.domain.models.common.Voters
import pl.masslany.podkop.business.links.domain.models.request.CommentsSortType
import pl.masslany.podkop.business.links.domain.models.request.LinksSortType
import pl.masslany.podkop.business.links.domain.models.request.LinksType


interface LinksRepository {
    @Suppress("LongParameterList")
    suspend fun getLinks(
        page: Any?,
        limit: Int?,
        linksSortType: LinksSortType,
        linksType: LinksType,
        category: String?,
        bucket: String?,
    ): Result<Resources>

    fun getLinksSortTypes(isUpcoming: Boolean): List<LinksSortType>

    fun getCommentsSortTypes(): List<CommentsSortType>

    suspend fun getLink(id: Int): Result<Link>

    @Suppress("LongParameterList")
    suspend fun getComments(
        id: Int,
        page: Int?,
        limit: Int?,
        commentSortType: CommentsSortType,
        ama: Boolean?,
    ): Result<Resources>

    suspend fun getSubComments(
        linkId: Int,
        commentId: Int,
        page: Int?,
    ): Result<Resources>

    suspend fun getRelatedLinks(linkId: Int): Result<Resources>

    suspend fun createLinkComment(
        linkId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem>

    suspend fun createLinkCommentReply(
        linkId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem>

    suspend fun updateLinkComment(
        linkId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<ResourceItem>

    suspend fun getLinkUpvotes(linkId: Int, type: String, page: Int?): Result<Voters>

    suspend fun voteOnLink(linkId: Int): Result<Unit>

    suspend fun removeVoteOnLink(linkId: Int): Result<Unit>

    suspend fun voteOnLinkComment(linkId: Int, commentId: Int): Result<Unit>

    suspend fun removeVoteOnLinkComment(linkId: Int, commentId: Int): Result<Unit>
}
