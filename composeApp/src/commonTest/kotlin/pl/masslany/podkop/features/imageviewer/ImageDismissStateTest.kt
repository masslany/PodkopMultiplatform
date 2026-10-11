package pl.masslany.podkop.features.imageviewer

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Velocity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

/** At a density of 1, so dp are pixels: letting go closes past 120 or faster than 900 a second. */
@OptIn(ExperimentalCoroutinesApi::class)
class ImageDismissStateTest {
    private val clock = BroadcastFrameClock()

    @Test
    fun `a slow release closes the viewer only past the distance, either way`() = runTest {
        assertFalse(createSut().dragAndRelease(dragY = 119f))
        assertTrue(createSut().dragAndRelease(dragY = 121f))
        assertTrue(createSut().dragAndRelease(dragY = -121f))
    }

    @Test
    fun `a fling away from the middle closes the viewer however short the drag`() = runTest {
        assertTrue(createSut().dragAndRelease(dragY = 20f, velocityY = 901f))
        assertTrue(createSut().dragAndRelease(dragY = -20f, velocityY = -901f))
    }

    @Test
    fun `flinging the image back keeps the viewer open however far it was dragged`() = runTest {
        assertFalse(createSut().dragAndRelease(dragY = 300f, velocityY = -901f))
        assertFalse(createSut().dragAndRelease(dragY = -300f, velocityY = 901f))
    }

    @Test
    fun `progress follows the vertical drag and stops at the end of the range`() = runTest {
        val sut = createSut()

        sut.dragBy(Offset(x = 50f, y = 0f))
        runCurrent()
        assertEquals(0f, sut.progress)

        sut.dragBy(Offset(x = 0f, y = -120f))
        runCurrent()
        assertEquals(0.5f, sut.progress)

        sut.dragBy(Offset(x = 0f, y = -360f))
        runCurrent()
        assertEquals(1f, sut.progress)
    }

    @Test
    fun `an image let go short of closing returns to its place`() = runTest {
        val sut = createSut()

        sut.dragBy(Offset(x = 10f, y = 60f))
        runCurrent()
        assertTrue(sut.isDragged)

        sut.release(Velocity.Zero, containerHeight = CONTAINER_HEIGHT)
        playAnimations()

        assertEquals(Offset.Zero, sut.offset)
        assertFalse(sut.isDragged)
    }

    @Test
    fun `an image let go to close leaves the screen the way it was dragged`() = runTest {
        val sut = createSut()

        sut.dragBy(Offset(x = 10f, y = -150f))
        sut.release(Velocity.Zero, containerHeight = CONTAINER_HEIGHT)
        playAnimations()

        assertEquals(Offset(x = 10f, y = -CONTAINER_HEIGHT), sut.offset)
    }

    @Test
    fun `a drag picks the image up where it is on its way back`() = runTest {
        val sut = createSut()

        sut.dragBy(Offset(x = 0f, y = 100f))
        sut.release(Velocity.Zero, containerHeight = CONTAINER_HEIGHT)
        runCurrent()
        clock.sendFrame(0L)
        runCurrent()
        clock.sendFrame(RETURN_PARTWAY_NANOS)
        runCurrent()
        val partway = sut.offset
        assertTrue(partway.y in 1f..99f, "Expected the image partway back, was at $partway")

        sut.dragBy(Offset(x = 0f, y = 10f))
        runCurrent()

        assertEquals(partway.y + 10f, sut.offset.y)
    }

    private fun TestScope.createSut(): ImageDismissState =
        ImageDismissState(scope = CoroutineScope(backgroundScope.coroutineContext + clock), density = Density(1f))

    private fun TestScope.playAnimations() {
        runCurrent()
        clock.sendFrame(0L)
        runCurrent()
        clock.sendFrame(ANIMATIONS_DONE_NANOS)
        runCurrent()
    }

    private fun ImageDismissState.dragAndRelease(
        dragY: Float,
        velocityY: Float = 0f,
    ): Boolean {
        dragBy(Offset(x = 0f, y = dragY))
        return release(Velocity(x = 0f, y = velocityY), containerHeight = CONTAINER_HEIGHT)
    }

    private companion object {
        const val CONTAINER_HEIGHT = 800f
        const val RETURN_PARTWAY_NANOS = 50_000_000L
        const val ANIMATIONS_DONE_NANOS = 10_000_000_000L
    }
}
