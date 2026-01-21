package pl.masslany.podkop.common.network.api

import pl.masslany.podkop.common.network.models.request.Request
import pl.masslany.podkop.common.network.models.response.ApiResponse
import pl.masslany.podkop.common.network.models.response.ResponseTypeInfo
import kotlin.reflect.typeOf

abstract class ApiClient {
    abstract suspend fun <T> executeRequest(
        request: Request<T>,
        responseType: ResponseTypeInfo,
    ): Result<ApiResponse<T>>
}

@OptIn(ExperimentalStdlibApi::class)
suspend inline fun <reified T> ApiClient.request(request: Request<T>): Result<ApiResponse<T>> {
    val responseType = typeOf<T>()

    return executeRequest(
        request = request,
        responseType =
            ResponseTypeInfo(
                type = T::class,
                kotlinType = responseType,
            ),
    )
}
