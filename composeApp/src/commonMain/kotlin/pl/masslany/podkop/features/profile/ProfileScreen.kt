package pl.masslany.podkop.features.profile

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class ProfileScreen(val username: String) : NavTarget
