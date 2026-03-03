package pl.masslany.podkop.common.composer.composermedia

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class ComposerMediaUrlDialogScreen(val resultKey: String) : NavTarget

data class ComposerMediaUrlDialogResult(val url: String?)
