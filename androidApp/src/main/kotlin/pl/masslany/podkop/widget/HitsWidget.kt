package pl.masslany.podkop.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProviders
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
import androidx.glance.material3.ColorProviders as MaterialColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import java.util.Date
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import pl.masslany.podkop.MainActivity
import pl.masslany.podkop.R
import pl.masslany.podkop.common.theme.darkColorScheme
import pl.masslany.podkop.common.theme.lightColorScheme

/** Home screen widget with the day's hits; the data comes from [HitsWidgetWorker] through [HitsWidgetStore]. */
class HitsWidget : GlanceAppWidget(), KoinComponent {
    private val store by inject<HitsWidgetStore>()

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        HitsWidgetWorker.ensureScheduled(context)
        if (store.load().needsRefresh(System.currentTimeMillis())) {
            HitsWidgetWorker.refreshNow(context)
        }

        provideContent {
            val state by store.state.collectAsState()
            val thumbnails = remember(state.snapshot) { loadThumbnails(state.snapshot) }
            GlanceTheme(colors = widgetColors()) {
                HitsWidgetContent(state = state, thumbnails = thumbnails)
            }
        }
    }

    private fun loadThumbnails(snapshot: HitsWidgetSnapshot?): Map<Int, Bitmap> = snapshot?.items
        .orEmpty()
        .filter { it.thumbnailUrl != null }
        .mapNotNull { item ->
            store.thumbnailFile(item.id)
                .takeIf { it.exists() }
                ?.let { BitmapFactory.decodeFile(it.path) }
                ?.let { item.id to it }
        }
        .toMap()
}

class HitsWidgetReceiver : GlanceAppWidgetReceiver(), KoinComponent {
    override val glanceAppWidget: GlanceAppWidget = HitsWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        HitsWidgetWorker.ensureScheduled(context)
        HitsWidgetWorker.refreshNow(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        HitsWidgetWorker.cancel(context)
        get<HitsWidgetStore>().clear()
    }
}

class RefreshHitsWidgetAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        HitsWidgetWorker.refreshNow(context)
    }
}

/** Dynamic colors where the system has them, the app's own palette before Android 12. */
@Composable
private fun widgetColors(): ColorProviders = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    GlanceTheme.colors
} else {
    MaterialColorProviders(light = lightColorScheme, dark = darkColorScheme)
}

@Composable
private fun HitsWidgetContent(state: HitsWidgetState, thumbnails: Map<Int, Bitmap>) {
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
        Header(
            updatedAtMillis = snapshot?.updatedAtMillis?.takeIf { width >= SHOW_UPDATED_AT_MIN_WIDTH },
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
                    HitRow(
                        item = item,
                        thumbnail = thumbnails[item.id],
                        showThumbnail = width >= SHOW_THUMBNAILS_MIN_WIDTH,
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(updatedAtMillis: Long?, isRefreshing: Boolean) {
    val context = LocalContext.current

    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .clickable(actionStartActivity(appIntent(context, HITS_DEEP_LINK))),
        ) {
            Text(
                text = context.getString(R.string.hits_widget_title),
                maxLines = 1,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
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

@Composable
private fun HitRow(item: HitsWidgetItem, thumbnail: Bitmap?, showThumbnail: Boolean) {
    val context = LocalContext.current

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(actionStartActivity(appIntent(context, LINK_DEEP_LINK_PREFIX + item.id))),
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
                MetaIcon(R.drawable.ic_widget_votes)
                MetaText(item.votes.toString())
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
private fun MetaIcon(resId: Int) {
    Image(
        provider = ImageProvider(resId),
        contentDescription = null,
        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
        modifier = GlanceModifier.size(12.dp),
    )
    Spacer(modifier = GlanceModifier.width(2.dp))
}

@Composable
private fun MetaText(text: String) {
    Text(
        text = text,
        maxLines = 1,
        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp),
    )
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

/** Opens [deepLink] in the app through the same handler as notifications and app links. */
private fun appIntent(context: Context, deepLink: String): Intent =
    Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        data = deepLink.toUri()
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

private const val HITS_DEEP_LINK = "https://masslany.pl/app/hits"
private const val LINK_DEEP_LINK_PREFIX = "https://masslany.pl/wykop/link/"
private val SHOW_THUMBNAILS_MIN_WIDTH = 220.dp
private val SHOW_UPDATED_AT_MIN_WIDTH = 160.dp
