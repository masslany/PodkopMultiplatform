package pl.masslany.podkop.features.resourceactions

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

internal fun Modifier.resourceTextSelectionGesture(
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
): Modifier = composed {
    when {
        onClick == null && onLongClick == null -> this

        onClick != null && onLongClick == null -> {
            clickable(onClick = onClick)
        }

        else -> {
            val interactionSource = remember { MutableInteractionSource() }

            combinedClickable(
                interactionSource = interactionSource,
                indication = if (onClick == null) null else LocalIndication.current,
                onClick = onClick ?: {},
                onLongClick = onLongClick,
            )
        }
    }
}
