package pl.masslany.podkop.common.composer.composermedia

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import pl.masslany.podkop.R
import pl.masslany.podkop.common.components.dialog.PodkopAlertDialog
import pl.masslany.podkop.common.navigation.AppNavigator

@Composable
fun ComposerMediaUrlDialogScreenRoot(
    screen: ComposerMediaUrlDialogScreen,
    appNavigator: AppNavigator,
    modifier: Modifier = Modifier,
) {
    var urlText by remember { mutableStateOf("") }
    PodkopAlertDialog(
        modifier = modifier,
        onDismissRequest = {
            appNavigator.sendResult(
                key = screen.resultKey,
                result = ComposerMediaUrlDialogResult(url = null),
            )
        },
        title = {
            Text(text = stringResource(id = R.string.composer_photo_url_dialog_title))
        },
        text = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = urlText,
                onValueChange = { urlText = it },
                singleLine = true,
                placeholder = {
                    Text(text = stringResource(id = R.string.composer_photo_url_dialog_hint))
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Uri,
                ),
            )
        },
        dismissButton = {
            TextButton(
                onClick = {
                    appNavigator.sendResult(
                        key = screen.resultKey,
                        result = ComposerMediaUrlDialogResult(url = null),
                    )
                },
            ) {
                Text(text = stringResource(id = R.string.dialog_button_dismiss))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    appNavigator.sendResult(
                        key = screen.resultKey,
                        result = ComposerMediaUrlDialogResult(url = urlText.trim()),
                    )
                },
            ) {
                Text(text = stringResource(id = R.string.composer_photo_url_dialog_confirm))
            }
        },
    )
}
