package pl.masslany.podkop.test.common

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class DisableAnimationsRule : TestRule {
    override fun apply(
        base: Statement,
        description: Description,
    ): Statement =
        object : Statement() {
            override fun evaluate() {
                val originalScales = readAnimationScales()
                setAnimationScales(AnimationScales.Disabled)
                try {
                    base.evaluate()
                } finally {
                    setAnimationScales(originalScales)
                }
            }
        }

    private fun readAnimationScales(): AnimationScales =
        with(UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())) {
            AnimationScales(
                transition = executeShellCommand("settings get global transition_animation_scale").trim(),
                window = executeShellCommand("settings get global window_animation_scale").trim(),
                animator = executeShellCommand("settings get global animator_duration_scale").trim(),
            )
        }

    private fun setAnimationScales(scales: AnimationScales) {
        with(UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())) {
            executeShellCommand("settings put global transition_animation_scale ${scales.transition}")
            executeShellCommand("settings put global window_animation_scale ${scales.window}")
            executeShellCommand("settings put global animator_duration_scale ${scales.animator}")
        }
    }
}

private data class AnimationScales(
    val transition: String,
    val window: String,
    val animator: String,
) {
    companion object {
        val Disabled = AnimationScales(
            transition = "0",
            window = "0",
            animator = "0",
        )
    }
}
