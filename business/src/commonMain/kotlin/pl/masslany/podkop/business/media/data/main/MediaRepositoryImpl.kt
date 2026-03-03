package pl.masslany.podkop.business.media.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.data.main.mapper.common.toPhoto
import pl.masslany.podkop.business.common.domain.models.common.Photo
import pl.masslany.podkop.business.media.data.api.MediaDataSource
import pl.masslany.podkop.business.media.domain.main.MediaPhotoType
import pl.masslany.podkop.business.media.domain.main.MediaRepository
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class MediaRepositoryImpl(
    private val mediaDataSource: MediaDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : MediaRepository {

    override suspend fun uploadPhotoFromDevice(
        bytes: ByteArray,
        fileName: String?,
        mimeType: String?,
        type: MediaPhotoType,
    ): Result<Photo> = withContext(dispatcherProvider.io) {
        mediaDataSource.uploadPhotoFromDevice(
            bytes = bytes,
            fileName = fileName,
            mimeType = mimeType,
            type = type.apiValue,
        ).mapCatching { it.toPhoto() }
    }

    override suspend fun uploadPhotoFromUrl(url: String, type: MediaPhotoType): Result<Photo> = withContext(dispatcherProvider.io) {
        mediaDataSource.uploadPhotoFromUrl(
            url = url,
            type = type.apiValue,
        )
            .mapCatching { it.toPhoto() }
    }

    override suspend fun deletePhoto(key: String): Result<Unit> = withContext(dispatcherProvider.io) {
        mediaDataSource.deletePhoto(key = key)
    }
}
