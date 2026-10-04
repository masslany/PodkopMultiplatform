package pl.masslany.podkop.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

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
