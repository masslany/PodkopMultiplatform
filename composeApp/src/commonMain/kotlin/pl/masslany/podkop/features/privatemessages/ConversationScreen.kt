package pl.masslany.podkop.features.privatemessages

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class ConversationScreen(val username: String) : NavTarget
