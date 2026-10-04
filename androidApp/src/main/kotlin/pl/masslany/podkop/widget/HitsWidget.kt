package pl.masslany.podkop.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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
            GlanceTheme(colors = hitsWidgetColors()) {
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
