package pl.masslany.podkop.business.blacklists.data.network.client

import pl.masslany.podkop.business.blacklists.data.network.api.BlacklistsApi
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistTagDataDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistTagRequestDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistUserDataDto
import pl.masslany.podkop.business.blacklists.data.network.models.BlacklistUserRequestDto
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request

class BlacklistsApiClient(
    private val apiClient: ApiClient,
) : BlacklistsApi {
    override suspend fun addBlacklistedUser(username: String): Result<Unit> {
        val request = Request<Unit>(
            method = Request.HttpMethod.POST,
            path = "api/v3/settings/blacklists/users",
            body = BlacklistUserRequestDto(
                data = BlacklistUserDataDto(username = username),
            ),
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun removeBlacklistedUser(username: String): Result<Unit> {
        val request = Request<Unit>(
            method = Request.HttpMethod.DELETE,
            path = "api/v3/settings/blacklists/users/$username",
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun addBlacklistedTag(tag: String): Result<Unit> {
        val request = Request<Unit>(
            method = Request.HttpMethod.POST,
            path = "api/v3/settings/blacklists/tags",
            body = BlacklistTagRequestDto(
                data = BlacklistTagDataDto(tag = normalizeTagForRequest(tag)),
            ),
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun removeBlacklistedTag(tag: String): Result<Unit> {
        val request = Request<Unit>(
            method = Request.HttpMethod.DELETE,
            path = "api/v3/settings/blacklists/tags/${tag.removePrefix("#")}",
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) },
        )
    }

    private fun normalizeTagForRequest(tag: String): String =
        if (tag.startsWith("#")) {
            tag
        } else {
            "#$tag"
        }
}
