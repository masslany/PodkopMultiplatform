package pl.masslany.podkop.common.composer.composermedia

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.navigation.AppNavigator
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.composer_photo_attach_device
import podkop.composeapp.generated.resources.composer_photo_attach_title
import podkop.composeapp.generated.resources.composer_photo_attach_url
import podkop.composeapp.generated.resources.dialog_button_dismiss

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerMediaAttachBottomSheetScreenRoot(
    screen: ComposerMediaAttachBottomSheetScreen,
    appNavigator: AppNavigator,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = {
            appNavigator.sendResult(
                key = screen.resultKey,
                result = ComposerMediaAttachResult.Dismissed,
            )
        },
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(resource = Res.string.composer_photo_attach_title),
                style = MaterialTheme.typography.titleMedium,
            )
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    appNavigator.sendResult(
                        key = screen.resultKey,
                        result = ComposerMediaAttachResult.Url,
                    )
                },
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(resource = Res.string.composer_photo_attach_url),
                )
            }
            if (screen.showLocalPicker) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        appNavigator.sendResult(
                            key = screen.resultKey,
                            result = ComposerMediaAttachResult.Local,
                        )
                    },
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(resource = Res.string.composer_photo_attach_device),
                    )
                }
            }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    appNavigator.sendResult(
                        key = screen.resultKey,
                        result = ComposerMediaAttachResult.Dismissed,
                    )
                },
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(resource = Res.string.dialog_button_dismiss),
                )
            }
        }
    }
}
