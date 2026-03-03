package pl.masslany.podkop.common.composer

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
internal actual fun rememberComposerImagePickerLauncher(
    onImagePicked: (ComposerPickedImage) -> Unit,
    onCancelled: () -> Unit,
): ComposerImagePickerLauncher {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val latestOnImagePicked by rememberUpdatedState(newValue = onImagePicked)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) {
            onCancelled()
            return@rememberLauncherForActivityResult
        }

        val contentResolver = context.contentResolver
        coroutineScope.launch(Dispatchers.IO) {
            val bytes = runCatching {
                contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }.getOrNull() ?: return@launch

            latestOnImagePicked(
                ComposerPickedImage(
                    bytes = bytes,
                    fileName = contentResolver.resolveDisplayName(uri),
                    mimeType = contentResolver.getType(uri),
                ),
            )
        }
    }

    return remember(launcher) {
        object : ComposerImagePickerLauncher {
            override val isAvailable: Boolean = true

            override fun launch() {
                launcher.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                    ),
                )
            }
        }
    }
}

internal actual fun isComposerImagePickerAvailable(): Boolean = true

private fun ContentResolver.resolveDisplayName(uri: Uri): String? {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    return query(uri, projection, null, null, null)?.use { cursor ->
        if (!cursor.moveToFirst()) {
            return@use null
        }

        val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (columnIndex < 0) {
            null
        } else {
            cursor.getString(columnIndex)
        }
    }
}
