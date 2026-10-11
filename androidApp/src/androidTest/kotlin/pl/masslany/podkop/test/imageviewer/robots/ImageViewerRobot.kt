package pl.masslany.podkop.test.imageviewer.robots

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.features.imageviewer.ImageViewerTestTags
import pl.masslany.podkop.test.common.PodkopComposeRule
import pl.masslany.podkop.test.common.robots.BaseRobot

class ImageViewerRobot(
    testRule: PodkopComposeRule,
) : BaseRobot(testRule) {
    fun displayImage() {
        displayedNode(ImageViewerTestTags.Screen.Image)
    }

    fun swipeImageAway() {
        touchNodeWithTag(ImageViewerTestTags.Screen.Image) {
            swipeDown(startY = centerY, endY = bottom)
        }
    }

    fun dragImageSlightly() {
        touchNodeWithTag(ImageViewerTestTags.Screen.Image) {
            down(center)
            repeat(SLIGHT_DRAG_STEPS) {
                advanceEventTime(STEP_MILLIS)
                moveBy(Offset(x = 0f, y = SLIGHT_DRAG_STEP.toPx()))
            }
            advanceEventTime(HOLD_MILLIS)
            up()
        }
    }

    fun assertClosed() {
        waitUntilNodeGone(ImageViewerTestTags.Screen.Image)
    }

    private companion object {
        // 60 dp in all: half the distance past which letting go closes the viewer.
        const val SLIGHT_DRAG_STEPS = 6
        val SLIGHT_DRAG_STEP = 10.dp
        const val STEP_MILLIS = 50L
        const val HOLD_MILLIS = 300L
    }
}

fun imageViewer(
    testRule: PodkopComposeRule,
    block: ImageViewerRobot.() -> Unit,
) = ImageViewerRobot(testRule).apply(block)
