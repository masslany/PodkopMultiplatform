package pl.masslany.podkop.common.settings

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.Flow

@Stable
interface AppSettings {
    val autoplayGifs: Flow<Boolean>

    suspend fun setAutoplayGifs(enabled: Boolean)
}
