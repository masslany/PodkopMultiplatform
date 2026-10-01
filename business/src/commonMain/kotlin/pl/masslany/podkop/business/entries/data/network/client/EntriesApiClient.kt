package pl.masslany.podkop.business.entries.data.network.client

import pl.masslany.podkop.business.common.data.network.client.putPagination
import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.entries.data.network.api.EntriesApi
import pl.masslany.podkop.business.entries.data.network.models.EntryCommentCreateDataDto
import pl.masslany.podkop.business.entries.data.network.models.EntryCommentCreateRequestDto
import pl.masslany.podkop.business.entries.data.network.models.EntryCreateDataDto
import pl.masslany.podkop.business.entries.data.network.models.EntryCreateRequestDto
import pl.masslany.podkop.business.entries.data.network.models.EntrySurveyVoteDataDto
import pl.masslany.podkop.business.entries.data.network.models.EntrySurveyVoteRequestDto
import pl.masslany.podkop.business.entries.data.network.models.EntryThreadReplyCreateDataDto
import pl.masslany.podkop.business.entries.data.network.models.EntryThreadReplyCreateRequestDto
import pl.masslany.podkop.business.entries.data.network.models.EntryThreadRepliesResponseDto
import pl.masslany.podkop.business.entries.data.network.models.EntryThreadResponseDto
import pl.masslany.podkop.business.entries.data.network.models.EntryVotersResponseDto
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request
import pl.masslany.podkop.common.pagination.PageRequest

class EntriesApiClient(
    private val apiClient: ApiClient,
) : EntriesApi {
    override suspend fun getEntries(
        page: PageRequest,
        limit: Int?,
        sort: String,
        hotSort: Int,
        category: String?,
        bucket: String?,
    ): Result<ResourceResponseDto> {
        val queryParameters = buildMap {
            put("sort", sort)
            put("last_update", hotSort.toString())
            putPagination(page)
            limit?.let { put("limit", it.toString()) }
            category?.let { put("category", it) }
            bucket?.let { put("bucket", it) }
        }

        val request =
            Request<ResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/entries",
                queryParameters = queryParameters,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getEntry(entryId: Int): Result<SingleResourceResponseDto> {
        val request =
            Request<SingleResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/entries/$entryId",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getEntryComments(
        entryId: Int,
        page: Int?,
    ): Result<ResourceResponseDto> {
        val queryParameters = buildMap {
            page?.let { putPagination(PageRequest.Number(it)) }
        }.takeIf { it.isNotEmpty() }

        val request =
            Request<ResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/entries/$entryId/comments",
                queryParameters = queryParameters,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getEntryThread(
        entryId: Int,
        sort: String,
        limit: Int,
    ): Result<EntryThreadResponseDto> {
        val request =
            Request<EntryThreadResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/entries-threads/$entryId",
                queryParameters = mapOf(
                    "comments_sort" to sort,
                    "comments_limit" to limit.toString(),
                    "comments_expanded" to "true",
                ),
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getEntryThreadReplies(
        entryId: Int,
        parentCommentId: Int?,
        sort: String,
        afterId: Int?,
        limit: Int,
    ): Result<EntryThreadRepliesResponseDto> {
        val queryParameters = buildMap {
            put("sort", sort)
            put("limit", limit.toString())
            put("expanded", "true")
            // Thread lists page by the id of the last loaded sibling, not by page number.
            afterId?.let { put("id", it.toString()) }
        }

        val request =
            Request<EntryThreadRepliesResponseDto>(
                method = Request.HttpMethod.GET,
                path = entryThreadCommentsPath(entryId, parentCommentId),
                queryParameters = queryParameters,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun createEntryThreadReply(
        entryId: Int,
        parentCommentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<EntryThreadResponseDto> {
        val body = EntryThreadReplyCreateRequestDto(
            data = EntryThreadReplyCreateDataDto(
                content = content,
                adult = adult,
                photos = photoKey?.let(::listOf),
            ),
        )
        val request =
            Request<EntryThreadResponseDto>(
                method = Request.HttpMethod.POST,
                path = entryThreadCommentsPath(entryId, parentCommentId),
                body = body,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    private fun entryThreadCommentsPath(entryId: Int, parentCommentId: Int?): String =
        if (parentCommentId == null) {
            "api/v3/entries-threads/$entryId/comments"
        } else {
            "api/v3/entries-threads/$entryId/comments/$parentCommentId/comments"
        }

    override suspend fun getEntryVotes(
        entryId: Int,
        page: Int?,
    ): Result<EntryVotersResponseDto> {
        val queryParameters = buildMap {
            page?.let { putPagination(PageRequest.Number(it)) }
        }.takeIf { it.isNotEmpty() }

        val request =
            Request<EntryVotersResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/entries/$entryId/votes",
                queryParameters = queryParameters,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getEntryCommentVotes(
        entryId: Int,
        commentId: Int,
        page: Int?,
    ): Result<EntryVotersResponseDto> {
        val queryParameters = buildMap {
            page?.let { putPagination(PageRequest.Number(it)) }
        }.takeIf { it.isNotEmpty() }

        val request =
            Request<EntryVotersResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/entries/$entryId/comments/$commentId/votes",
                queryParameters = queryParameters,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun createEntryComment(
        entryId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        val body = EntryCommentCreateRequestDto(
            data = EntryCommentCreateDataDto(
                content = content,
                adult = adult,
                photo = photoKey,
            ),
        )
        val request =
            Request<SingleResourceResponseDto>(
                method = Request.HttpMethod.POST,
                path = "api/v3/entries/$entryId/comments",
                body = body,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun createEntry(
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        val body = EntryCreateRequestDto(
            data = EntryCreateDataDto(
                content = content,
                adult = adult,
                photo = photoKey,
            ),
        )
        val request =
            Request<SingleResourceResponseDto>(
                method = Request.HttpMethod.POST,
                path = "api/v3/entries",
                body = body,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun updateEntry(
        entryId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        val body = EntryCreateRequestDto(
            data = EntryCreateDataDto(
                content = content,
                adult = adult,
                photo = photoKey,
            ),
        )
        val request = Request<SingleResourceResponseDto>(
            method = Request.HttpMethod.PUT,
            path = "api/v3/entries/$entryId",
            body = body,
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun updateEntryComment(
        entryId: Int,
        commentId: Int,
        content: String,
        adult: Boolean,
        photoKey: String?,
    ): Result<SingleResourceResponseDto> {
        val body = EntryCommentCreateRequestDto(
            data = EntryCommentCreateDataDto(
                content = content,
                adult = adult,
                photo = photoKey,
            ),
        )
        val request = Request<SingleResourceResponseDto>(
            method = Request.HttpMethod.PUT,
            path = "api/v3/entries/$entryId/comments/$commentId",
            body = body,
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun voteUp(entryId: Int): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.POST,
                path = "api/v3/entries/$entryId/votes",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun voteSurvey(
        entryId: Int,
        optionNumber: Int,
    ): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.POST,
                path = "api/v3/entries/$entryId/survey/votes",
                body = EntrySurveyVoteRequestDto(
                    data = EntrySurveyVoteDataDto(vote = optionNumber),
                ),
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun removeVoteUp(entryId: Int): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/entries/$entryId/votes",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun deleteEntry(entryId: Int): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/entries/$entryId",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun deleteEntryComment(entryId: Int, commentId: Int): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/entries/$entryId/comments/$commentId",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun voteUpComment(entryId: Int, commentId: Int): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.POST,
                path = "api/v3/entries/$entryId/comments/$commentId/votes",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun removeVoteUpComment(entryId: Int, commentId: Int): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/entries/$entryId/comments/$commentId/votes",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
