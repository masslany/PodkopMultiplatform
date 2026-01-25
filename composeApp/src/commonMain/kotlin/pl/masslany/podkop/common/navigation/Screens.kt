package pl.masslany.podkop.common.navigation

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data object MainApp : MainAppTarget

/**
 * A Universal Dialog implementation.
 * @param key A unique ID used to map the result back to the caller.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class GenericDialog(
    val title: String,
    val description: String? = null,
    val positiveText: String = "OK",
    val negativeText: String? = null,
    val key: String = Uuid.random().toString()
) : NavTarget
