package pl.masslany.podkop.widget

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import kotlinx.collections.immutable.ImmutableMap
import pl.masslany.podkop.R
import pl.masslany.podkop.widget.components.HitsWidgetHeader
import pl.masslany.podkop.widget.components.HitsWidgetMessage
import pl.masslany.podkop.widget.components.HitsWidgetRow
import pl.masslany.podkop.widget.models.HitsWidgetState

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
            snapshot == null && state.lastRefreshFailed -> HitsWidgetMessage(
                text = context.getString(R.string.hits_widget_error),
                onClick = actionRunCallback<RefreshHitsWidgetAction>(),
            )

            snapshot == null -> HitsWidgetMessage(text = context.getString(R.string.hits_widget_loading))

            snapshot.items.isEmpty() -> HitsWidgetMessage(text = context.getString(R.string.hits_widget_empty))

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

private val ShowThumbnailsMinWidth = 220.dp
private val ShowUpdatedAtMinWidth = 160.dp
