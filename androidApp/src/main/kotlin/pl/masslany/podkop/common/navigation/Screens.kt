package pl.masslany.podkop.common.navigation

import androidx.annotation.StringRes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
data object HomeScreen : NavTarget

@Serializable
sealed interface DialogText {
    /**
     * Stores already resolved display text directly in the navigation payload.
     */
    @Serializable
    data class Raw(val value: String) : DialogText

    /** A string resource localized in the UI layer (`DefaultGenericDialog`) with `stringResource(...)`. */
    @Serializable
    data class Resource(@StringRes val id: Int, val args: List<String> = emptyList()) : DialogText
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
         */
        @OptIn(ExperimentalUuidApi::class)
        fun fromResources(
            @StringRes title: Int,
            @StringRes description: Int? = null,
            @StringRes positiveText: Int? = null,
            @StringRes negativeText: Int? = null,
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

fun dialogText(@StringRes resource: Int, vararg args: String): DialogText =
    DialogText.Resource(
        id = resource,
        args = args.toList(),
    )

fun dialogText(value: String): DialogText = DialogText.Raw(value)
