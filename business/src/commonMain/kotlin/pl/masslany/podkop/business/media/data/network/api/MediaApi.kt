package pl.masslany.podkop.business.media.data.network.api

import pl.masslany.podkop.business.common.data.network.models.common.PhotoDto

interface MediaApi {
    suspend fun uploadPhotoFromDevice(
        bytes: ByteArray,
        fileName: String?,
        mimeType: String?,
        type: String,
    ): Result<PhotoDto>

    suspend fun uploadPhotoFromUrl(url: String, type: String): Result<PhotoDto>

    suspend fun deletePhoto(key: String): Result<Unit>
}
