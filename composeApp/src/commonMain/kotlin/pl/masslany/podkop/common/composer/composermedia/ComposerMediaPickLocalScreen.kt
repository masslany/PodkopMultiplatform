package pl.masslany.podkop.common.composer.composermedia

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.composer.ComposerPickedImage
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class ComposerMediaPickLocalScreen(val resultKey: String) : NavTarget

data class ComposerMediaPickLocalResult(val image: ComposerPickedImage?)
