package pl.masslany.podkop.business.media.data.network.client

import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import pl.masslany.podkop.business.common.data.network.models.common.PhotoDto
import pl.masslany.podkop.business.media.data.network.api.MediaApi
import pl.masslany.podkop.business.media.data.network.models.MediaPhotoFromUrlDataDto
import pl.masslany.podkop.business.media.data.network.models.MediaPhotoFromUrlRequestDto
import pl.masslany.podkop.business.media.data.network.models.MediaPhotoResponseDto
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request

class MediaApiClient(
    private val apiClient: ApiClient,
) : MediaApi {

    override suspend fun uploadPhotoFromDevice(
        bytes: ByteArray,
        fileName: String?,
        mimeType: String?,
        type: String,
    ): Result<PhotoDto> {
        val body = MultiPartFormDataContent(
            formData {
                append(
                    key = "file",
                    value = bytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, mimeType ?: DEFAULT_IMAGE_MIME_TYPE)
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=\"${resolveFileName(fileName)}\"",
                        )
                    },
                )
            },
        )
        val request = Request<MediaPhotoResponseDto>(
            method = Request.HttpMethod.POST,
            path = "api/v3/media/photos/upload",
            queryParameters = mapOf("type" to type),
            body = body,
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content.data) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun uploadPhotoFromUrl(url: String, type: String): Result<PhotoDto> {
        val request = Request<MediaPhotoResponseDto>(
            method = Request.HttpMethod.POST,
            path = "api/v3/media/photos",
            queryParameters = mapOf("type" to type),
            body = MediaPhotoFromUrlRequestDto(
                data = MediaPhotoFromUrlDataDto(
                    url = url,
                ),
            ),
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content.data) },
            onFailure = { Result.failure(it) },
        )
    }

    override suspend fun deletePhoto(key: String): Result<Unit> {
        val request = Request<Unit>(
            method = Request.HttpMethod.DELETE,
            path = "api/v3/media/photos/$key",
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }

    private fun resolveFileName(fileName: String?): String {
        val normalized = fileName
            ?.trim()
            ?.substringAfterLast('/')
            ?.substringAfterLast('\\')
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_UPLOAD_FILE_NAME

        return normalized.replace("\"", "")
    }

    private companion object {
        const val DEFAULT_UPLOAD_FILE_NAME = "upload.jpg"
        const val DEFAULT_IMAGE_MIME_TYPE = "image/jpeg"
    }
}
