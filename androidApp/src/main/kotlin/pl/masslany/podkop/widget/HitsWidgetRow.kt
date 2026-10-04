package pl.masslany.podkop.widget

import android.graphics.Bitmap
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
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import pl.masslany.podkop.R
import pl.masslany.podkop.common.theme.DarkHotOrange
import pl.masslany.podkop.common.theme.LightHotOrange

@Composable
internal fun HitsWidgetRow(item: HitsWidgetItem, thumbnail: Bitmap?, showThumbnail: Boolean) {
    val context = LocalContext.current

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(actionStartActivity(openLinkIntent(context, item.id))),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showThumbnail) {
            Box(
                modifier = GlanceModifier
                    .size(width = 56.dp, height = 42.dp)
                    .cornerRadius(8.dp)
                    .background(GlanceTheme.colors.surfaceVariant),
            ) {
                if (thumbnail != null) {
                    Image(
                        provider = ImageProvider(thumbnail),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = GlanceModifier.fillMaxSize().cornerRadius(8.dp),
                    )
                }
            }
            Spacer(modifier = GlanceModifier.width(10.dp))
        }
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = item.title,
                maxLines = 2,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val votesColor = if (item.isHot) HotVotesColor else GlanceTheme.colors.onSurfaceVariant
                MetaIcon(R.drawable.ic_widget_votes, color = votesColor)
                MetaText(item.votes.toString(), color = votesColor)
                Spacer(modifier = GlanceModifier.width(8.dp))
                MetaIcon(R.drawable.ic_widget_comments)
                MetaText(item.comments.toString())
                if (item.source != null) {
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    MetaText(item.source)
                }
            }
        }
    }
}

@Composable
private fun MetaIcon(resId: Int, color: ColorProvider = GlanceTheme.colors.onSurfaceVariant) {
    Image(
        provider = ImageProvider(resId),
        contentDescription = null,
        colorFilter = ColorFilter.tint(color),
        modifier = GlanceModifier.size(12.dp),
    )
    Spacer(modifier = GlanceModifier.width(2.dp))
}

@Composable
private fun MetaText(text: String, color: ColorProvider = GlanceTheme.colors.onSurfaceVariant) {
    Text(
        text = text,
        maxLines = 1,
        style = TextStyle(color = color, fontSize = 11.sp),
    )
}

/** The app's orange for vote counts on hot content, as in its count badges. */
private val HotVotesColor = ColorProvider(day = LightHotOrange, night = DarkHotOrange)
