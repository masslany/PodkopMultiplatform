package pl.masslany.podkop.business.media.data.network.main

import pl.masslany.podkop.business.common.data.network.models.common.PhotoDto
import pl.masslany.podkop.business.media.data.api.MediaDataSource
import pl.masslany.podkop.business.media.data.network.api.MediaApi

class MediaDataSourceImpl(
    private val mediaApi: MediaApi,
) : MediaDataSource {
    override suspend fun uploadPhotoFromDevice(
        bytes: ByteArray,
        fileName: String?,
        mimeType: String?,
        type: String,
    ): Result<PhotoDto> {
        return mediaApi.uploadPhotoFromDevice(
            bytes = bytes,
            fileName = fileName,
            mimeType = mimeType,
            type = type,
        )
    }

    override suspend fun uploadPhotoFromUrl(url: String, type: String): Result<PhotoDto> {
        return mediaApi.uploadPhotoFromUrl(
            url = url,
            type = type,
        )
    }

    override suspend fun deletePhoto(key: String): Result<Unit> {
        return mediaApi.deletePhoto(key = key)
    }
}
