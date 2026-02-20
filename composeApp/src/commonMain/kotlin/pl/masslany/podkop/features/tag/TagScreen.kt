package pl.masslany.podkop.features.tag

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class TagScreen(val tag: String) : NavTarget
