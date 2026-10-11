package pl.masslany.podkop.test.fixtures

import android.graphics.Bitmap
import android.graphics.Color
import java.io.ByteArrayOutputStream

object ImageFixtures {
    fun png(): ByteArray {
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.GRAY)
        return ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    }
}
