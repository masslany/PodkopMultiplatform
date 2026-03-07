package pl.masslany.podkop.features.privatemessages.inbox

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
internal actual fun PrivateMessagesNotificationPermissionEffect(
    shouldRequestPermission: Boolean,
    onPermissionResult: (Boolean) -> Unit,
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onPermissionResult,
    )

    LaunchedEffect(shouldRequestPermission) {
        if (shouldRequestPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
