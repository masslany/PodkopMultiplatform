package pl.masslany.podkop.business.links.data.network.client

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.links.data.network.api.LinksApi
import pl.masslany.podkop.business.links.data.network.models.LinkCommentCreateDataDto
import pl.masslany.podkop.business.links.data.network.models.LinkCommentCreateRequestDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftCheckResponseDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftCreateDataDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftCreateRequestDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftResponseDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftsResponseDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftPublishDataDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftPublishRequestDto
import pl.masslany.podkop.business.links.data.network.models.LinkUpvotesResponseDto
import pl.masslany.podkop.business.links.domain.models.request.PublishLinkDraft
import pl.masslany.podkop.business.links.domain.models.request.UpdateLinkDraft
import pl.masslany.podkop.business.links.data.network.models.LinkDraftUpdateDataDto
import pl.masslany.podkop.business.links.data.network.models.LinkDraftUpdateRequestDto
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request

class LinksApiClient(
    private val apiClient: ApiClient,
) : LinksApi {
    override suspend fun getLinks(
        page: Any?,
        limit: Int?,
        sort: String,
        type: String,
        category: String?,
        bucket: String?,
    ): Result<ResourceResponseDto> {
        val queryParams =
            mutableMapOf(
                "sort" to sort,
                "type" to type,
            )
        page?.let { queryParams["page"] = it.toString() }
        limit?.let { queryParams["limit"] = it.toString() }
        category?.let { queryParams["category"] = it }
        bucket?.let { queryParams["bucket"] = it }

        val request =
            Request<ResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/links",
                queryParameters = queryParams,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getLink(id: Int): Result<SingleResourceResponseDto> {
        val request =
            Request<SingleResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/links/$id",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getComments(
        id: Int,
        page: Int?,
        limit: Int?,
        sort: String?,
        ama: Boolean?,
    ): Result<ResourceResponseDto> {
        val queryParams = mutableMapOf<String, String>()
        page?.let { queryParams["page"] = it.toString() }
        limit?.let { queryParams["limit"] = it.toString() }
        sort?.let { queryParams["sort"] = it }
        ama?.let { queryParams["ama"] = it.toString() }

        val request =
            Request<ResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/links/$id/comments",
                queryParameters = queryParams,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getSubComments(
        linkId: Int,
        commentId: Int,
        page: Int?,
    ): Result<ResourceResponseDto> {
        val queryParams = mutableMapOf<String, String>()
        page?.let { queryParams["page"] = it.toString() }

        val request =
            Request<ResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/links/$linkId/comments/$commentId/comments",
                queryParameters = queryParams,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getRelatedLinks(linkId: Int): Result<ResourceResponseDto> {
        val request =
            Request<ResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/links/$linkId/related",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun createLinkDraft(url: String): Result<LinkDraftCheckResponseDto> {
        val request = Request<LinkDraftCheckResponseDto>(
            method = Request.HttpMethod.POST,
            path = "api/v3/links/draft",
            body = LinkDraftCreateRequestDto(
                data = LinkDraftCreateDataDto(url = url),
            ),
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getLinkDrafts(): Result<LinkDraftsResponseDto> {
        val request = Request<LinkDraftsResponseDto>(
            method = Request.HttpMethod.GET,
            path = "api/v3/links/draft",
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getLinkDraft(key: String): Result<LinkDraftResponseDto> {
        val request = Request<LinkDraftResponseDto>(
            method = Request.HttpMethod.GET,
            path = "api/v3/links/draft/$key",
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun updateLinkDraft(key: String, request: UpdateLinkDraft): Result<Unit> {
        val body = LinkDraftUpdateRequestDto(
            data = LinkDraftUpdateDataDto(
                title = request.title,
                description = request.description,
                tags = request.tags,
                photo = request.photoKey,
                adult = request.adult,
                selectedImage = request.selectedImageIndex,
            ),
        )
        val apiRequest = Request<Unit>(
            method = Request.HttpMethod.PUT,
            path = "api/v3/links/draft/$key",
            body = body,
        )

        return apiClient.request(apiRequest).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun deleteLinkDraft(key: String): Result<Unit> {
        val request = Request<Unit>(
            method = Request.HttpMethod.DELETE,
            path = "api/v3/links/draft/$key",
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun publishLinkDraft(key: String, request: PublishLinkDraft): Result<Unit> {
        val body = LinkDraftPublishRequestDto(
            data = LinkDraftPublishDataDto(
                title = request.title,
                description = request.description,
                tags = request.tags,
                photo = request.photoKey,
                adult = request.adult,
                selectedImage = request.selectedImageIndex,
            ),
        )
        val apiRequest = Request<Unit>(
            method = Request.HttpMethod.POST,
            path = "api/v3/links/draft/$key",
            body = body,
        )

        return apiClient.request(apiRequest).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun createLinkComment(
        linkId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        val body = LinkCommentCreateRequestDto(
            data = LinkCommentCreateDataDto(
                content = content,
                adult = adult,
                photo = photoKey,
            ),
        )
        val request =
            Request<SingleResourceResponseDto>(
                method = Request.HttpMethod.POST,
                path = "api/v3/links/$linkId/comments",
                body = body,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun createLinkCommentReply(
        linkId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        val body = LinkCommentCreateRequestDto(
            data = LinkCommentCreateDataDto(
                content = content,
                adult = adult,
                photo = photoKey,
            ),
        )
        val request =
            Request<SingleResourceResponseDto>(
                method = Request.HttpMethod.POST,
                path = "api/v3/links/$linkId/comments/$commentId/comments",
                body = body,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun updateLinkComment(
        linkId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        val body = LinkCommentCreateRequestDto(
            data = LinkCommentCreateDataDto(
                content = content,
                adult = adult,
                photo = photoKey,
            ),
        )
        val request = Request<SingleResourceResponseDto>(
            method = Request.HttpMethod.PUT,
            path = "api/v3/links/$linkId/comments/$commentId",
            body = body,
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getLinkUpvotes(linkId: Int, type: String, page: Int?): Result<LinkUpvotesResponseDto> {
        val queryParams = mutableMapOf<String, String>()
        page?.let { queryParams["page"] = it.toString() }

        val request =
            Request<LinkUpvotesResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/links/$linkId/upvotes/$type",
                queryParameters = queryParams,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun voteOnLink(linkId: Int): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.POST,
                path = "api/v3/links/$linkId/votes/up",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun removeVoteOnLink(linkId: Int): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/links/$linkId/votes",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun voteUpOnRelatedLink(linkId: Int, relatedId: Int): Result<Unit> {
        return voteOnRelatedLink(linkId = linkId, relatedId = relatedId, direction = "up")
    }

    override suspend fun voteDownOnRelatedLink(linkId: Int, relatedId: Int): Result<Unit> {
        return voteOnRelatedLink(linkId = linkId, relatedId = relatedId, direction = "down")
    }

    private suspend fun voteOnRelatedLink(
        linkId: Int,
        relatedId: Int,
        direction: String,
    ): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.POST,
                path = "api/v3/links/$linkId/related/$relatedId/votes/$direction",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun removeVoteOnRelatedLink(linkId: Int, relatedId: Int): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/links/$linkId/related/$relatedId/votes",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun voteOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        return voteOnLinkComment(linkId = linkId, commentId = commentId, direction = "up")
    }

    override suspend fun voteDownOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        return voteOnLinkComment(linkId = linkId, commentId = commentId, direction = "down")
    }

    private suspend fun voteOnLinkComment(
        linkId: Int,
        commentId: Int,
        direction: String,
    ): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.POST,
                path = "api/v3/links/$linkId/comments/$commentId/votes/$direction",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun removeVoteOnLinkComment(linkId: Int, commentId: Int): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/links/$linkId/comments/$commentId/votes",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
