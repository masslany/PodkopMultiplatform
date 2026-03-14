package pl.masslany.podkop.features.resourceactions

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class ResourceTextSelectionDialogScreen(val content: String, val previewDraftId: String? = null) : NavTarget

data class ResourceTextSelectionDialogParams(val content: String, val previewDraftId: String? = null)
