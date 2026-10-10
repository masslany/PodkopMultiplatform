package pl.masslany.podkop.features.imageviewer

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImageViewerDismissTest {
    @Test
    fun `a slow release closes the viewer only past the distance, either way`() {
        assertFalse(closesOnRelease(dragY = 119f, velocityY = 0f, minDistance = DISTANCE, minVelocity = VELOCITY))
        assertTrue(closesOnRelease(dragY = 121f, velocityY = 0f, minDistance = DISTANCE, minVelocity = VELOCITY))
        assertTrue(closesOnRelease(dragY = -121f, velocityY = 0f, minDistance = DISTANCE, minVelocity = VELOCITY))
    }

    @Test
    fun `a fling away from the middle closes the viewer however short the drag`() {
        assertTrue(closesOnRelease(dragY = 20f, velocityY = 901f, minDistance = DISTANCE, minVelocity = VELOCITY))
        assertTrue(closesOnRelease(dragY = -20f, velocityY = -901f, minDistance = DISTANCE, minVelocity = VELOCITY))
    }

    @Test
    fun `flinging the image back keeps the viewer open however far it was dragged`() {
        assertFalse(closesOnRelease(dragY = 300f, velocityY = -901f, minDistance = DISTANCE, minVelocity = VELOCITY))
        assertFalse(closesOnRelease(dragY = -300f, velocityY = 901f, minDistance = DISTANCE, minVelocity = VELOCITY))
    }

    @Test
    fun `progress follows the vertical drag and stops at the end of the range`() {
        assertEquals(0f, dismissProgress(drag = Offset(x = 50f, y = 0f), range = RANGE))
        assertEquals(0.5f, dismissProgress(drag = Offset(x = 0f, y = -120f), range = RANGE))
        assertEquals(1f, dismissProgress(drag = Offset(x = 0f, y = 480f), range = RANGE))
    }

    private companion object {
        const val DISTANCE = 120f
        const val VELOCITY = 900f
        const val RANGE = 240f
    }
}
