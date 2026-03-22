package pl.masslany.podkop.business.profile.data.network.client

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.profile.data.network.api.ProfileApi
import pl.masslany.podkop.business.profile.data.network.models.ObservedTagsResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ObservedUsersResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileBadgesResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileNoteResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileNoteUpdateDataDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileNoteUpdateRequestDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileShortDto
import pl.masslany.podkop.business.profile.data.network.models.UsersAutoCompleteResponseDto
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request

class ProfileApiClient(
    private val apiClient: ApiClient,
) : ProfileApi {
    override suspend fun getProfileShort(): Result<ProfileShortDto> {
        val request =
            Request<ProfileShortDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/profile/short",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getProfile(): Result<ProfileDto> {
        val request =
            Request<ProfileDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/profile",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getProfile(name: String): Result<ProfileDto> {
        val request =
            Request<ProfileDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/profile/users/$name",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getProfileBadges(username: String): Result<ProfileBadgesResponseDto> {
        val request =
            Request<ProfileBadgesResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/profile/users/$username/badges",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getProfileNote(username: String): Result<ProfileNoteResponseDto> {
        val request =
            Request<ProfileNoteResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/notes/$username",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun updateProfileNote(
        username: String,
        content: String,
    ): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.PUT,
                path = "api/v3/notes/$username",
                body = ProfileNoteUpdateRequestDto(
                    data = ProfileNoteUpdateDataDto(content = content),
                ),
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun observeUser(username: String): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.POST,
                path = "api/v3/observed/users/$username",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun unobserveUser(username: String): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/observed/users/$username",
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getUsersAutoComplete(query: String): Result<UsersAutoCompleteResponseDto> {
        val queryParameters = buildMap {
            put("query", query)
        }
        val request =
            Request<UsersAutoCompleteResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/users/autocomplete",
                queryParameters = queryParameters,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun getProfileActions(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileList(
            username = username,
            page = page,
            endpoint = "actions",
        )
    }

    override suspend fun getProfileEntriesAdded(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileList(
            username = username,
            page = page,
            endpoint = "entries/added",
        )
    }

    override suspend fun getProfileEntriesVoted(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileList(
            username = username,
            page = page,
            endpoint = "entries/voted",
        )
    }

    override suspend fun getProfileEntriesCommented(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileList(
            username = username,
            page = page,
            endpoint = "entries/commented",
        )
    }

    override suspend fun getProfileLinksAdded(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileList(
            username = username,
            page = page,
            endpoint = "links/added",
        )
    }

    override suspend fun getProfileLinksPublished(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileList(
            username = username,
            page = page,
            endpoint = "links/published",
        )
    }

    override suspend fun getProfileLinksUp(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileList(
            username = username,
            page = page,
            endpoint = "links/up",
        )
    }

    override suspend fun getProfileLinksDown(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileList(
            username = username,
            page = page,
            endpoint = "links/down",
        )
    }

    override suspend fun getProfileLinksCommented(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileList(
            username = username,
            page = page,
            endpoint = "links/commented",
        )
    }

    override suspend fun getProfileLinksRelated(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileList(
            username = username,
            page = page,
            endpoint = "links/related",
        )
    }

    override suspend fun getProfileObservedTags(
        username: String,
        page: Int,
    ): Result<ObservedTagsResponseDto> {
        return getProfileList(
            username = username,
            page = page,
            endpoint = "observed/tags",
        )
    }

    override suspend fun getProfileObservedUsersFollowing(
        username: String,
        page: Int,
    ): Result<ObservedUsersResponseDto> {
        return getProfileList(
            username = username,
            page = page,
            endpoint = "observed/users/following",
        )
    }

    override suspend fun getProfileObservedUsersFollowers(
        username: String,
        page: Int,
    ): Result<ObservedUsersResponseDto> {
        return getProfileList(
            username = username,
            page = page,
            endpoint = "observed/users/followers",
        )
    }

    private suspend inline fun <reified T> getProfileList(
        username: String,
        page: Int,
        endpoint: String,
    ): Result<T> {
        val queryParameters = buildMap {
            put("page", page.toString())
        }

        val request =
            Request<T>(
                method = Request.HttpMethod.GET,
                path = "api/v3/profile/users/$username/$endpoint",
                queryParameters = queryParameters,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
