package pl.masslany.podkop.features.resourceactions

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class ResourceScreenshotPreviewDialogScreen(val draftId: String) : NavTarget
