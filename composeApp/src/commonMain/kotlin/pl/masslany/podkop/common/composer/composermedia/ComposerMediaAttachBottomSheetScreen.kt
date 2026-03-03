package pl.masslany.podkop.common.composer.composermedia

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class ComposerMediaAttachBottomSheetScreen(val resultKey: String, val showLocalPicker: Boolean) : NavTarget

enum class ComposerMediaAttachResult {
    Url,
    Local,
    Dismissed,
}
