package pl.masslany.podkop.widget

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import kotlinx.collections.immutable.ImmutableMap
import pl.masslany.podkop.R

@Composable
internal fun HitsWidgetContent(state: HitsWidgetState, thumbnails: ImmutableMap<Int, Bitmap>) {
    val context = LocalContext.current
    val width = LocalSize.current.width
    val snapshot = state.snapshot

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(16.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        HitsWidgetHeader(
            updatedAtMillis = snapshot?.updatedAtMillis?.takeIf { width >= ShowUpdatedAtMinWidth },
            isRefreshing = state.isRefreshing,
        )
        when {
            snapshot == null && state.lastRefreshFailed -> Message(
                text = context.getString(R.string.hits_widget_error),
                onClick = actionRunCallback<RefreshHitsWidgetAction>(),
            )

            snapshot == null -> Message(text = context.getString(R.string.hits_widget_loading))

            snapshot.items.isEmpty() -> Message(text = context.getString(R.string.hits_widget_empty))

            else -> LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(snapshot.items, itemId = { it.id.toLong() }) { item ->
                    HitsWidgetRow(
                        item = item,
                        thumbnail = thumbnails[item.id],
                        showThumbnail = width >= ShowThumbnailsMinWidth,
                    )
                }
            }
        }
    }
}

@Composable
private fun Message(text: String, onClick: Action? = null) {
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

private val ShowThumbnailsMinWidth = 220.dp
private val ShowUpdatedAtMinWidth = 160.dp
