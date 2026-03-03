package pl.masslany.podkop.common.composer.composermedia

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.navigation.AppNavigator
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.composer_photo_url_dialog_confirm
import podkop.composeapp.generated.resources.composer_photo_url_dialog_hint
import podkop.composeapp.generated.resources.composer_photo_url_dialog_title
import podkop.composeapp.generated.resources.dialog_button_dismiss

@Composable
fun ComposerMediaUrlDialogScreenRoot(
    screen: ComposerMediaUrlDialogScreen,
    appNavigator: AppNavigator,
    modifier: Modifier = Modifier,
) {
    var urlText by remember { mutableStateOf("") }
    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            appNavigator.sendResult(
                key = screen.resultKey,
                result = ComposerMediaUrlDialogResult(url = null),
            )
        },
        title = {
            Text(text = stringResource(resource = Res.string.composer_photo_url_dialog_title))
        },
        text = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = urlText,
                onValueChange = { urlText = it },
                singleLine = true,
                placeholder = {
                    Text(text = stringResource(resource = Res.string.composer_photo_url_dialog_hint))
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
                Text(text = stringResource(resource = Res.string.dialog_button_dismiss))
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
                Text(text = stringResource(resource = Res.string.composer_photo_url_dialog_confirm))
            }
        },
    )
}
