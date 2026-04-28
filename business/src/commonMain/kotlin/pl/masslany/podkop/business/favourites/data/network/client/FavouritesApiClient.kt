package pl.masslany.podkop.business.favourites.data.network.client

import pl.masslany.podkop.business.common.data.network.client.putPagination
import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.favourites.data.network.api.FavouritesApi
import pl.masslany.podkop.business.favourites.data.network.models.FavouriteRequestDataDto
import pl.masslany.podkop.business.favourites.data.network.models.FavouriteRequestDto
import pl.masslany.podkop.business.favourites.domain.models.FavouriteType
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request
import pl.masslany.podkop.common.pagination.PageRequest

class FavouritesApiClient(
    private val apiClient: ApiClient,
) : FavouritesApi {
    override suspend fun getFavourites(
        page: PageRequest,
        sort: String,
        resource: String?,
    ): Result<ResourceResponseDto> {
        val queryParameters = buildMap {
            put("sort", sort)
            putPagination(page)
            resource?.let { put("resource", it) }
        }

        val request =
            Request<ResourceResponseDto>(
                method = Request.HttpMethod.GET,
                path = buildString {
                    append("api/v3/favourites")
                },
                queryParameters = queryParameters,
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun createFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.POST,
                path = "api/v3/favourites",
                body = FavouriteRequestDto(
                    data = FavouriteRequestDataDto(
                        type = type.value,
                        sourceId = sourceId,
                    ),
                ),
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun deleteFavourite(
        type: FavouriteType,
        sourceId: Int,
    ): Result<Unit> {
        val request =
            Request<Unit>(
                method = Request.HttpMethod.DELETE,
                path = "api/v3/favourites",
                body = FavouriteRequestDto(
                    data = FavouriteRequestDataDto(
                        type = type.value,
                        sourceId = sourceId,
                    ),
                ),
            )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
