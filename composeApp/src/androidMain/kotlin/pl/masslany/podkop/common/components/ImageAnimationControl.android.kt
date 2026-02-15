package pl.masslany.podkop.common.components

import android.graphics.drawable.Animatable
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import coil3.DrawableImage
import coil3.Image

internal actual fun setImageAnimationPlaying(
    image: Image,
    isPlaying: Boolean,
) {
    val drawable = (image as? DrawableImage)?.drawable ?: return

    when (drawable) {
        is Animatable2Compat -> {
            if (isPlaying) {
                drawable.start()
            } else {
                drawable.stop()
            }
        }

        is Animatable -> {
            if (isPlaying) {
                drawable.start()
            } else {
                drawable.stop()
            }
        }
    }
}
