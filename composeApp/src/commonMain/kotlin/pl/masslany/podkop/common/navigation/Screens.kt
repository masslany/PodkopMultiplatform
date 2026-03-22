package pl.masslany.podkop.common.navigation

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
data object HomeScreen : NavTarget

@Serializable
sealed interface DialogText {
    /**
     * Stores already resolved display text directly in the navigation payload.
     */
    @Serializable
    data class Raw(val value: String) : DialogText

    /**
     * Stores a Compose resource key (not [org.jetbrains.compose.resources.StringResource]) because
     * [GenericDialog] is serialized as a navigation target and `StringResource` is not serializable.
     *
     * The key is resolved back to `StringResource` in the UI layer (`DefaultGenericDialog`) using
     * the generated `Res.allStringResources` registry, then localized with `stringResource(...)`.
     */
    @Serializable
    data class Resource(val key: String, val args: List<String> = emptyList()) : DialogText
}

/**
 * A Universal Dialog implementation.
 * @param key A unique ID used to map the result back to the caller.
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class GenericDialog(
    val title: DialogText,
    val description: DialogText? = null,
    val positiveText: DialogText = DialogText.Raw("OK"),
    val negativeText: DialogText? = null,
    val key: String = Uuid.random().toString(),
) : NavTarget {
    companion object {
        /**
         * Convenience factory for ViewModels: lets them create localized dialog payloads without
         * resolving strings outside Compose.
         *
         * Each `StringResource` is converted to a serializable [DialogText.Resource] key and later
         * resolved in the composable dialog renderer.
         */
        @OptIn(ExperimentalUuidApi::class)
        fun fromResources(
            title: StringResource,
            description: StringResource? = null,
            positiveText: StringResource? = null,
            negativeText: StringResource? = null,
            key: String = Uuid.random().toString(),
        ): GenericDialog = GenericDialog(
            title = dialogText(title),
            description = description?.let(::dialogText),
            positiveText = positiveText?.let(::dialogText) ?: DialogText.Raw("OK"),
            negativeText = negativeText?.let(::dialogText),
            key = key,
        )
    }
}

/**
 * Converts a Compose [StringResource] into a serializable dialog payload representation.
 */
fun dialogText(resource: StringResource, vararg args: String): DialogText =
    DialogText.Resource(
        key = resource.key,
        args = args.toList(),
    )

fun dialogText(value: String): DialogText = DialogText.Raw(value)
