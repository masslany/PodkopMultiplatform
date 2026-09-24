package pl.masslany.podkop.features.imageviewer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pl.masslany.podkop.common.snackbar.SnackbarMessage
import pl.masslany.podkop.testsupport.fakes.FakeSnackbarManager
import pl.masslany.podkop.testsupport.navigation.createTestAppNavigator
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.snackbar_generic_error
import podkop.composeapp.generated.resources.snackbar_image_saved
import podkop.composeapp.generated.resources.snackbar_screenshot_copied

@OptIn(ExperimentalCoroutinesApi::class)
class ImageViewerViewModelTest {
    @Test
    fun `copy exports the source only once while busy and reports completion`() = runViewModelTest {
        val completed = CompletableDeferred<Boolean>()
        val service = FakeImageExportService().apply { copy = { completed.await() } }
        val snackbar = FakeSnackbarManager()
        val sut = createSut(service, snackbar)

        sut.onCopyClicked(IMAGE_URL)
        sut.onCopyClicked(IMAGE_URL)
        sut.onDownloadClicked(IMAGE_URL)
        runCurrent()

        assertTrue(sut.state.value.isCopying)
        assertEquals(listOf(IMAGE_URL), service.copiedUrls)
        assertTrue(service.downloadedUrls.isEmpty())
        assertTrue(snackbar.emittedEvents.isEmpty())

        completed.complete(true)
        advanceUntilIdle()

        assertFalse(sut.state.value.isCopying)
        assertEquals(
            SnackbarMessage.Resource(Res.string.snackbar_screenshot_copied),
            snackbar.emittedEvents.single().message,
        )
    }

    @Test
    fun `failed image loads and clipboard errors allow retry`() = runViewModelTest {
        val service = FakeImageExportService().apply { copy = { false } }
        val snackbar = FakeSnackbarManager()
        val sut = createSut(service, snackbar)

        sut.onCopyClicked(IMAGE_URL)
        advanceUntilIdle()
        assertFalse(sut.state.value.isCopying)
        assertEquals(SnackbarMessage.Resource(Res.string.snackbar_generic_error), snackbar.emittedEvents.last().message)

        service.copy = { error("Clipboard unavailable") }
        sut.onCopyClicked(IMAGE_URL)
        advanceUntilIdle()
        assertFalse(sut.state.value.isCopying)
        assertEquals(SnackbarMessage.Resource(Res.string.snackbar_generic_error), snackbar.emittedEvents.last().message)

        service.copy = { true }
        sut.onCopyClicked(IMAGE_URL)
        advanceUntilIdle()
        assertEquals(3, service.copiedUrls.size)
        assertEquals(
            SnackbarMessage.Resource(Res.string.snackbar_screenshot_copied),
            snackbar.emittedEvents.last().message,
        )
    }

    @Test
    fun `cancellation clears progress without showing an error`() = runViewModelTest {
        val service = FakeImageExportService().apply { copy = { throw CancellationException() } }
        val snackbar = FakeSnackbarManager()
        val sut = createSut(service, snackbar)

        sut.onCopyClicked(IMAGE_URL)
        advanceUntilIdle()

        assertFalse(sut.state.value.isCopying)
        assertTrue(snackbar.emittedEvents.isEmpty())
    }

    @Test
    fun `sheet stays open while copying and closes only when export finishes`() = runViewModelTest {
        val navigator = createTestAppNavigator(backgroundScope)
        navigator.initialize()
        runCurrent()
        navigator.navigateTo(ImageActionsBottomSheetScreen(IMAGE_URL))
        val completed = CompletableDeferred<Boolean>()
        val service = FakeImageExportService().apply { copy = { completed.await() } }
        val snackbar = FakeSnackbarManager()
        val sut = ImageViewerViewModel(IMAGE_URL, navigator, service, snackbar, dismissAfterAction = true)

        sut.onCopyClicked(IMAGE_URL)
        runCurrent()
        assertEquals(ImageActionsBottomSheetScreen(IMAGE_URL), navigator.state.value.rootStack.last())

        completed.complete(true)
        advanceUntilIdle()
        assertFalse(navigator.state.value.rootStack.last() is ImageActionsBottomSheetScreen)
        assertEquals(1, snackbar.emittedEvents.size)
    }

    @Test
    fun `download reports success and failure`() = runViewModelTest {
        val service = FakeImageExportService()
        val snackbar = FakeSnackbarManager()
        val sut = createSut(service, snackbar)
        sut.onDownloadClicked(IMAGE_URL)
        assertEquals(listOf(IMAGE_URL), service.downloadedUrls)
        assertEquals(SnackbarMessage.Resource(Res.string.snackbar_image_saved), snackbar.emittedEvents.last().message)

        service.downloadResult = false
        sut.onDownloadClicked(IMAGE_URL)
        assertEquals(SnackbarMessage.Resource(Res.string.snackbar_generic_error), snackbar.emittedEvents.last().message)
    }

    private fun TestScope.createSut(service: ImageExportService, snackbar: FakeSnackbarManager) = ImageViewerViewModel(
        imageUrl = IMAGE_URL,
        appNavigator = createTestAppNavigator(backgroundScope),
        imageExportService = service,
        snackbarManager = snackbar,
    )

    private fun runViewModelTest(block: suspend TestScope.() -> Unit) {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        try {
            runTest(dispatcher, testBody = block)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private const val IMAGE_URL = "https://example.com/full-size-image.jpg"

private class FakeImageExportService : ImageExportService {
    val copiedUrls = mutableListOf<String>()
    val downloadedUrls = mutableListOf<String>()
    var copy: suspend () -> Boolean = { true }
    var downloadResult = true

    override suspend fun copyImage(url: String): Boolean {
        copiedUrls += url
        return copy()
    }

    override fun downloadImage(url: String): Boolean {
        downloadedUrls += url
        return downloadResult
    }
}
