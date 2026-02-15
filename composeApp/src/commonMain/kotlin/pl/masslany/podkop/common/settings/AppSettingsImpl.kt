package pl.masslany.podkop.common.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import pl.masslany.podkop.common.persistence.api.KeyValueStorage

class AppSettingsImpl(private val keyValueStorage: KeyValueStorage) : AppSettings {
    override val autoplayGifs: Flow<Boolean> =
        keyValueStorage.observeBoolean(AUTOPLAY_GIFS_KEY)
            .map { it ?: true }
            .distinctUntilChanged()

    override suspend fun setAutoplayGifs(enabled: Boolean) {
        keyValueStorage.putBoolean(AUTOPLAY_GIFS_KEY, enabled)
    }

    private companion object {
        const val AUTOPLAY_GIFS_KEY = "AUTOPLAY_GIFS"
    }
}
