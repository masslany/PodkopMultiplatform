package pl.masslany.podkop.widget.models

import kotlinx.serialization.Serializable

/** The day's hits as the widget last fetched them; thumbnails live next to it as files named by link id. */
@Serializable
data class HitsWidgetSnapshot(
    val updatedAtMillis: Long,
    val items: List<HitsWidgetItem>,
) {
    companion object {
        const val MAX_ITEMS = 10
    }
}
