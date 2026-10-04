package pl.masslany.podkop.common.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.navigation.AnimatedDialogDismissHandler

/**
 * Material 3's alert dialog without a window of its own, for destinations shown by
 * `AnimatedDialogSceneStrategy`, which provides the window, scrim and motion. Material's
 * `AlertDialog` opens a second window that the scene could not animate.
 *
 * [onDismissRequest] runs when the dialog is dismissed by Back or a tap outside it.
 */
@Composable
internal fun PodkopAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
) {
    AnimatedDialogDismissHandler(onDismissRequest)
    Surface(
        modifier = modifier.widthIn(min = 280.dp, max = 560.dp),
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation,
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            title?.let {
                CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.titleContentColor) {
                    ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                        Box(modifier = Modifier.padding(bottom = 16.dp)) { it() }
                    }
                }
            }
            text?.let {
                CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.textContentColor) {
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                        Box(modifier = Modifier.padding(bottom = 24.dp)) { it() }
                    }
                }
            }
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
                ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                    FlowRow(
                        modifier = Modifier.align(Alignment.End),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        dismissButton?.invoke()
                        confirmButton()
                    }
                }
            }
        }
    }
}
