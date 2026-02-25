package pl.masslany.podkop.common.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.DialogText
import pl.masslany.podkop.common.navigation.GenericDialog
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.allStringResources

@Composable
internal fun DefaultGenericDialog(
    dialog: GenericDialog,
    navigator: AppNavigator,
) {
    AlertDialog(
        onDismissRequest = { navigator.back() },
        title = { Text(dialog.title.resolve()) },
        text = dialog.description?.let { dialogText ->
            {
                Text(
                    text = dialogText.resolve(),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    navigator.sendResult(dialog.key, true)
                },
            ) {
                Text(dialog.positiveText.resolve())
            }
        },
        dismissButton = if (dialog.negativeText != null) {
            {
                TextButton(
                    onClick = {
                        navigator.sendResult(dialog.key, false)
                    },
                ) {
                    Text(dialog.negativeText.resolve())
                }
            }
        } else {
            null
        },
    )
}

@Composable
private fun DialogText.resolve(): String =
    when (this) {
        is DialogText.Raw -> value

        // `DialogText.Resource` stores only a serializable resource key. We need to restore the
        // generated `StringResource` instance before calling `stringResource(...)`.
        // `Res.allStringResources` is the Compose-generated registry for this module.
        is DialogText.Resource -> stringResource(
            resource = requireNotNull(Res.allStringResources[key]) {
                "Missing string resource for key: $key"
            },
        )
    }
