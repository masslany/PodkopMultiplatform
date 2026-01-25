package pl.masslany.podkop.common.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.common.navigation.GenericDialog


@Composable
fun DefaultGenericDialog(
    dialog: GenericDialog,
    navigator: AppNavigator
) {
    AlertDialog(
        onDismissRequest = { navigator.back() },
        title = { Text(dialog.title) },
        text = dialog.description?.let { { Text(it) } },
        confirmButton = {
            TextButton(onClick = {
                // Send TRUE as result
                navigator.sendResult(dialog.key, true)
            }) {
                Text(dialog.positiveText)
            }
        },
        dismissButton = if (dialog.negativeText != null) {
            {
                TextButton(onClick = {
                    // Send FALSE as result
                    navigator.sendResult(dialog.key, false)
                }) {
                    Text(dialog.negativeText)
                }
            }
        } else null
    )
}