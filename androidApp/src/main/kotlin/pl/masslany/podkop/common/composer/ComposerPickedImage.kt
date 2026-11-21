package pl.masslany.podkop.common.composer

import androidx.compose.runtime.Stable

@Stable
data class ComposerPickedImage(val bytes: ByteArray, val fileName: String?, val mimeType: String?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ComposerPickedImage

        if (!bytes.contentEquals(other.bytes)) return false
        if (fileName != other.fileName) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + (fileName?.hashCode() ?: 0)
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        return result
    }
}

@Stable
internal interface ComposerImagePickerLauncher {
    fun launch()
}
