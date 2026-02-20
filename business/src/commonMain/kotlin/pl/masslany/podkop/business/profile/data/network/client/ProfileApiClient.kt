package pl.masslany.podkop.business.profile.data.network.client

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.profile.data.network.api.ProfileApi
import pl.masslany.podkop.business.profile.data.network.models.ProfileDto
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

    override suspend fun getUsersAutoComplete(query: String): Result<UsersAutoCompleteResponseDto> {
        val queryParams =
            mutableMapOf(
                "query" to query,
            )
        val request =
            Request<UsersAutoCompleteResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/users/autocomplete",
                queryParameters = queryParams,
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
        return getProfileResources(
            username = username,
            page = page,
            endpoint = "actions",
        )
    }

    override suspend fun getProfileEntriesAdded(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileResources(
            username = username,
            page = page,
            endpoint = "entries/added",
        )
    }

    override suspend fun getProfileEntriesVoted(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileResources(
            username = username,
            page = page,
            endpoint = "entries/voted",
        )
    }

    override suspend fun getProfileEntriesCommented(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileResources(
            username = username,
            page = page,
            endpoint = "entries/commented",
        )
    }

    override suspend fun getProfileLinksAdded(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileResources(
            username = username,
            page = page,
            endpoint = "links/added",
        )
    }

    override suspend fun getProfileLinksPublished(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileResources(
            username = username,
            page = page,
            endpoint = "links/published",
        )
    }

    override suspend fun getProfileLinksUp(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileResources(
            username = username,
            page = page,
            endpoint = "links/up",
        )
    }

    override suspend fun getProfileLinksDown(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileResources(
            username = username,
            page = page,
            endpoint = "links/down",
        )
    }

    override suspend fun getProfileLinksCommented(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileResources(
            username = username,
            page = page,
            endpoint = "links/commented",
        )
    }

    override suspend fun getProfileLinksRelated(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileResources(
            username = username,
            page = page,
            endpoint = "links/related",
        )
    }

    override suspend fun getProfileObservedTags(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileResources(
            username = username,
            page = page,
            endpoint = "observed/tags",
        )
    }

    override suspend fun getProfileObservedUsersFollowing(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileResources(
            username = username,
            page = page,
            endpoint = "observed/users/following",
        )
    }

    override suspend fun getProfileObservedUsersFollowers(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return getProfileResources(
            username = username,
            page = page,
            endpoint = "observed/users/followers",
        )
    }

    private suspend fun getProfileResources(
        username: String,
        page: Int,
        endpoint: String,
    ): Result<ResourceResponseDto> {
        val queryParams =
            mutableMapOf(
                "page" to page.toString(),
            )

        val request =
            Request<ResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/profile/users/$username/$endpoint",
                queryParameters = queryParams,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
