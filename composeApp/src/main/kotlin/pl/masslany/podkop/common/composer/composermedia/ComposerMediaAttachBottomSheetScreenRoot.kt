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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.navigation.AppNavigator
import pl.masslany.podkop.composeapp.R

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
                text = stringResource(id = R.string.composer_photo_attach_title),
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
                    text = stringResource(id = R.string.composer_photo_attach_url),
                )
            }
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
                    text = stringResource(id = R.string.composer_photo_attach_device),
                )
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
                    text = stringResource(id = R.string.dialog_button_dismiss),
                )
            }
        }
    }
}
