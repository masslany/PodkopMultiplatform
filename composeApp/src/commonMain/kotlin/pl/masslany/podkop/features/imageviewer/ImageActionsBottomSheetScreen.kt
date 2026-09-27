package pl.masslany.podkop.features.imageviewer

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class ImageActionsBottomSheetScreen(val imageUrl: String) : NavTarget
