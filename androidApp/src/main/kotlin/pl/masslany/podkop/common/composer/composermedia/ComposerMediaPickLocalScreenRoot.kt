package pl.masslany.podkop.common.composer.composermedia

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import pl.masslany.podkop.common.components.ScreenLoadingIndicator
import pl.masslany.podkop.common.composer.rememberComposerImagePickerLauncher
import pl.masslany.podkop.common.navigation.AppNavigator

@Composable
fun ComposerMediaPickLocalScreenRoot(
    screen: ComposerMediaPickLocalScreen,
    appNavigator: AppNavigator,
    modifier: Modifier = Modifier,
) {
    val pickerLauncher = rememberComposerImagePickerLauncher(
        onImagePicked = { image ->
            appNavigator.sendResult(
                key = screen.resultKey,
                result = ComposerMediaPickLocalResult(image = image),
            )
        },
        onCancelled = {
            appNavigator.sendResult(
                key = screen.resultKey,
                result = ComposerMediaPickLocalResult(image = null),
            )
        },
    )
    LaunchedEffect(Unit) {
        pickerLauncher.launch()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        ScreenLoadingIndicator()
    }
}
