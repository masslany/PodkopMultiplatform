package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.common.data.network.models.common.PhotoDto
import pl.masslany.podkop.business.media.data.api.MediaDataSource

class FakeMediaDataSource : MediaDataSource {
    data class UploadPhotoFromDeviceCall(
        val bytes: ByteArray,
        val fileName: String?,
        val mimeType: String?,
        val type: String,
    )

    data class UploadPhotoFromUrlCall(
        val url: String,
        val type: String,
    )

    var uploadPhotoFromDeviceResult: Result<PhotoDto> = unstubbedResult("MediaDataSource.uploadPhotoFromDevice")
    var uploadPhotoFromUrlResult: Result<PhotoDto> = unstubbedResult("MediaDataSource.uploadPhotoFromUrl")
    var deletePhotoResult: Result<Unit> = unstubbedResult("MediaDataSource.deletePhoto")

    val uploadPhotoFromDeviceCalls = mutableListOf<UploadPhotoFromDeviceCall>()
    val uploadPhotoFromUrlCalls = mutableListOf<UploadPhotoFromUrlCall>()
    val deletePhotoCalls = mutableListOf<String>()

    override suspend fun uploadPhotoFromDevice(
        bytes: ByteArray,
        fileName: String?,
        mimeType: String?,
        type: String,
    ): Result<PhotoDto> {
        uploadPhotoFromDeviceCalls += UploadPhotoFromDeviceCall(
            bytes = bytes,
            fileName = fileName,
            mimeType = mimeType,
            type = type,
        )
        return uploadPhotoFromDeviceResult
    }

    override suspend fun uploadPhotoFromUrl(url: String, type: String): Result<PhotoDto> {
        uploadPhotoFromUrlCalls += UploadPhotoFromUrlCall(
            url = url,
            type = type,
        )
        return uploadPhotoFromUrlResult
    }

    override suspend fun deletePhoto(key: String): Result<Unit> {
        deletePhotoCalls += key
        return deletePhotoResult
    }
}
