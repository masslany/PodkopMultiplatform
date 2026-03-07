package pl.masslany.podkop.features.privatemessages.inbox

import androidx.compose.runtime.Composable

@Composable
internal expect fun PrivateMessagesNotificationPermissionEffect(
    shouldRequestPermission: Boolean,
    onPermissionResult: (Boolean) -> Unit,
)
