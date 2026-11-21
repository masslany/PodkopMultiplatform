package pl.masslany.podkop.common.components.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.DialogText
import pl.masslany.podkop.common.navigation.GenericDialog

@Composable
internal fun DefaultGenericDialog(
    modifier: Modifier = Modifier,
    dialog: GenericDialog,
    navigator: AppNavigator,
) {
    PodkopAlertDialog(
        modifier = modifier,
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
        is DialogText.Resource -> stringResource(id, *args.toTypedArray())
    }
