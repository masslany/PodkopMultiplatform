package pl.masslany.podkop.widget.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle

/** Centered loading, empty or error text in place of the list, optionally tappable. */
@Composable
internal fun HitsWidgetMessage(text: String, onClick: Action? = null) {
    val modifier = GlanceModifier.fillMaxSize().padding(8.dp)
    Box(
        modifier = if (onClick != null) modifier.clickable(onClick) else modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
