package pl.masslany.podkop.features.search

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class AdvancedSearchScreen(val initialQuery: String = "") : NavTarget
