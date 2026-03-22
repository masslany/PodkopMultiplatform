package pl.masslany.podkop.business.entries.data.network.client

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.entries.data.network.api.EntriesApi
import pl.masslany.podkop.business.entries.data.network.models.EntryCommentCreateDataDto
import pl.masslany.podkop.business.entries.data.network.models.EntryCommentCreateRequestDto
import pl.masslany.podkop.business.entries.data.network.models.EntryCreateDataDto
import pl.masslany.podkop.business.entries.data.network.models.EntryCreateRequestDto
import pl.masslany.podkop.business.entries.data.network.models.EntrySurveyVoteDataDto
import pl.masslany.podkop.business.entries.data.network.models.EntrySurveyVoteRequestDto
import pl.masslany.podkop.business.entries.data.network.models.EntryVotersResponseDto
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request

class EntriesApiClient(
    private val apiClient: ApiClient,
) : EntriesApi {
    override suspend fun getEntries(
        page: Any?,
        limit: Int?,
        sort: String,
        hotSort: Int,
        category: String?,
        bucket: String?,
    ): Result<ResourceResponseDto> {
        val queryParameters = buildMap {
            put("sort", sort)
            put("last_update", hotSort.toString())
            page?.let { put("page", it.toString()) }
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
        page: Any?,
    ): Result<ResourceResponseDto> {
        val queryParameters = buildMap {
            page?.let { put("page", it.toString()) }
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

    override suspend fun getEntryVotes(
        entryId: Int,
        page: Any?,
    ): Result<EntryVotersResponseDto> {
        val queryParameters = buildMap {
            page?.let { put("page", it.toString()) }
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
        page: Any?,
    ): Result<EntryVotersResponseDto> {
        val queryParameters = buildMap {
            page?.let { put("page", it.toString()) }
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
