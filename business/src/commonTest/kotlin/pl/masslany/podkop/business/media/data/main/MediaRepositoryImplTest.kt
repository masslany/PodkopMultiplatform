package pl.masslany.podkop.business.media.data.main

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import pl.masslany.podkop.business.media.domain.main.MediaPhotoType
import pl.masslany.podkop.business.testsupport.fakes.FakeDispatcherProvider
import pl.masslany.podkop.business.testsupport.fakes.FakeMediaDataSource
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class MediaRepositoryImplTest {

    @Test
    fun `upload photo from url forwards url and maps response`() = runBlocking {
        val mediaDataSource = FakeMediaDataSource().apply {
            uploadPhotoFromUrlResult = Result.success(
                Fixtures.photoDto(
                    key = "photo-key-url",
                    url = "https://example.com/url-photo.png",
                ),
            )
        }
        val sut = createSut(mediaDataSource = mediaDataSource)

        val actual = sut.uploadPhotoFromUrl(
            url = "https://example.com/source.png",
            type = MediaPhotoType.Comments,
        )

        assertEquals(
            listOf(
                FakeMediaDataSource.UploadPhotoFromUrlCall(
                    url = "https://example.com/source.png",
                    type = "comments",
                ),
            ),
            mediaDataSource.uploadPhotoFromUrlCalls,
        )
        assertEquals(
            Fixtures.photo(
                key = "photo-key-url",
                url = "https://example.com/url-photo.png",
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `upload photo from url propagates failure`() = runBlocking {
        val expected = IllegalStateException("url upload failed")
        val mediaDataSource = FakeMediaDataSource().apply {
            uploadPhotoFromUrlResult = Result.failure(expected)
        }
        val sut = createSut(mediaDataSource = mediaDataSource)

        val actual = sut.uploadPhotoFromUrl(
            url = "https://example.com/source.png",
            type = MediaPhotoType.Comments,
        )

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
    }

    @Test
    fun `upload photo from device forwards payload and maps response`() = runBlocking {
        val payload = byteArrayOf(1, 2, 3, 4)
        val mediaDataSource = FakeMediaDataSource().apply {
            uploadPhotoFromDeviceResult = Result.success(
                Fixtures.photoDto(
                    key = "photo-key-device",
                    url = "https://example.com/device-photo.png",
                    mimeType = "image/png",
                ),
            )
        }
        val sut = createSut(mediaDataSource = mediaDataSource)

        val actual = sut.uploadPhotoFromDevice(
            bytes = payload,
            fileName = "image.png",
            mimeType = "image/png",
            type = MediaPhotoType.Links,
        )

        assertEquals(1, mediaDataSource.uploadPhotoFromDeviceCalls.size)
        val call = mediaDataSource.uploadPhotoFromDeviceCalls.first()
        assertContentEquals(payload, call.bytes)
        assertEquals("image.png", call.fileName)
        assertEquals("image/png", call.mimeType)
        assertEquals("links", call.type)
        assertEquals(
            Fixtures.photo(
                key = "photo-key-device",
                url = "https://example.com/device-photo.png",
                mimeType = "image/png",
            ),
            actual.getOrThrow(),
        )
    }

    @Test
    fun `upload photo from device propagates failure`() = runBlocking {
        val expected = IllegalArgumentException("invalid file")
        val mediaDataSource = FakeMediaDataSource().apply {
            uploadPhotoFromDeviceResult = Result.failure(expected)
        }
        val sut = createSut(mediaDataSource = mediaDataSource)

        val actual = sut.uploadPhotoFromDevice(
            bytes = byteArrayOf(9),
            fileName = "x.jpg",
            mimeType = "image/jpeg",
            type = MediaPhotoType.Comments,
        )

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
    }

    @Test
    fun `delete photo forwards key`() = runBlocking {
        val mediaDataSource = FakeMediaDataSource().apply {
            deletePhotoResult = Result.success(Unit)
        }
        val sut = createSut(mediaDataSource = mediaDataSource)

        val actual = sut.deletePhoto(key = "photo-key-delete")

        assertTrue(actual.isSuccess)
        assertEquals(listOf("photo-key-delete"), mediaDataSource.deletePhotoCalls)
    }

    @Test
    fun `delete photo propagates failure`() = runBlocking {
        val expected = IllegalStateException("delete failed")
        val mediaDataSource = FakeMediaDataSource().apply {
            deletePhotoResult = Result.failure(expected)
        }
        val sut = createSut(mediaDataSource = mediaDataSource)

        val actual = sut.deletePhoto(key = "photo-key-delete")

        assertTrue(actual.isFailure)
        assertSame(expected, actual.exceptionOrNull())
    }

    private fun createSut(
        mediaDataSource: FakeMediaDataSource = FakeMediaDataSource(),
    ): MediaRepositoryImpl {
        return MediaRepositoryImpl(
            mediaDataSource = mediaDataSource,
            dispatcherProvider = FakeDispatcherProvider(),
        )
    }
}
