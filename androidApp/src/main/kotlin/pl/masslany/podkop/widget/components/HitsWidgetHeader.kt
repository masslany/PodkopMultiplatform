package pl.masslany.podkop.widget.components

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import pl.masslany.podkop.R
import pl.masslany.podkop.widget.RefreshHitsWidgetAction
import pl.masslany.podkop.widget.openHitsIntent
import java.util.Date

@Composable
internal fun HitsWidgetHeader(updatedAtMillis: Long?, isRefreshing: Boolean) {
    val context = LocalContext.current

    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .clickable(actionStartActivity(openHitsIntent(context))),
        ) {
            Row {
                Image(
                    provider = ImageProvider(R.drawable.ic_fire),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                    modifier = GlanceModifier
                        .padding(end = 8.dp),
                )
                Text(
                    text = context.getString(R.string.hits_widget_title),
                    maxLines = 1,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
            if (updatedAtMillis != null) {
                Text(
                    text = context.getString(
                        R.string.hits_widget_updated_at,
                        DateFormat.getTimeFormat(context).format(Date(updatedAtMillis)),
                    ),
                    maxLines = 1,
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp),
                )
            }
        }
        Box(modifier = GlanceModifier.size(36.dp), contentAlignment = Alignment.Center) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = GlanceModifier.size(20.dp),
                    color = GlanceTheme.colors.primary,
                )
            } else {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_refresh),
                    contentDescription = context.getString(R.string.hits_widget_refresh),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                    modifier = GlanceModifier
                        .size(36.dp)
                        .padding(8.dp)
                        .clickable(actionRunCallback<RefreshHitsWidgetAction>()),
                )
            }
        }
    }
}
