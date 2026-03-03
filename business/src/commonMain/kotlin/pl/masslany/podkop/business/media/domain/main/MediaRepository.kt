package pl.masslany.podkop.business.media.domain.main

import pl.masslany.podkop.business.common.domain.models.common.Photo

interface MediaRepository {
    suspend fun uploadPhotoFromDevice(
        bytes: ByteArray,
        fileName: String?,
        mimeType: String?,
        type: MediaPhotoType,
    ): Result<Photo>

    suspend fun uploadPhotoFromUrl(url: String, type: MediaPhotoType): Result<Photo>

    suspend fun deletePhoto(key: String): Result<Unit>
}
