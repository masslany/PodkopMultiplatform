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
        val queryParams = mutableMapOf<String, String>()
        queryParams["page"] = page.toString()

        val request =
            Request<ResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = "api/v3/profile/users/$username/actions",
                queryParameters = queryParams,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
