package pl.masslany.podkop.features.entrydetails

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class EntryDetailsScreen(val id: Int) : NavTarget
