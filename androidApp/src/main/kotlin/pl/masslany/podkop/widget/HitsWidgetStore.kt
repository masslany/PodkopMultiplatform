package pl.masslany.podkop.widget

import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * Keeps the widget's snapshot and thumbnails on disk, so every widget instance and process restart
 * shows the same data, and publishes it as [state] for the running widget sessions.
 */
class HitsWidgetStore(
    private val directory: File,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    private val mutex = Mutex()
    private var isLoaded = false
    private val _state = MutableStateFlow(HitsWidgetState())
    val state: StateFlow<HitsWidgetState> = _state.asStateFlow()

    private val snapshotFile: File get() = File(directory, SNAPSHOT_FILE_NAME)

    /** Reads the saved snapshot on first use in this process; later calls return the current state. */
    suspend fun load(): HitsWidgetState = mutex.withLock {
        if (!isLoaded) {
            isLoaded = true
            val snapshot = runCatching {
                json.decodeFromString<HitsWidgetSnapshot>(snapshotFile.readText())
            }.getOrNull()
            _state.update { it.copy(snapshot = snapshot) }
        }
        _state.value
    }

    fun thumbnailFile(linkId: Int): File = File(directory, "$linkId$THUMBNAIL_SUFFIX")

    fun setRefreshing(isRefreshing: Boolean) {
        _state.update { it.copy(isRefreshing = isRefreshing) }
    }

    /** Keeps the last snapshot on screen and lets the widget offer a retry when there is none. */
    fun markRefreshFailed() {
        _state.update { it.copy(isRefreshing = false, lastRefreshFailed = true) }
    }

    /** Saves [snapshot] with the [thumbnails] that downloaded, keyed by link id, and drops unused ones. */
    suspend fun save(snapshot: HitsWidgetSnapshot, thumbnails: Map<Int, ByteArray>) = mutex.withLock {
        directory.mkdirs()
        thumbnails.forEach { (linkId, bytes) -> thumbnailFile(linkId).writeBytes(bytes) }

        val pendingFile = File(directory, "$SNAPSHOT_FILE_NAME.tmp")
        pendingFile.writeText(json.encodeToString(HitsWidgetSnapshot.serializer(), snapshot))
        if (!pendingFile.renameTo(snapshotFile)) {
            snapshotFile.writeText(pendingFile.readText())
            pendingFile.delete()
        }

        val listedIds = snapshot.items.mapTo(mutableSetOf()) { it.id }
        directory.listFiles()
            ?.filter { it.name.endsWith(THUMBNAIL_SUFFIX) }
            ?.filter { it.name.removeSuffix(THUMBNAIL_SUFFIX).toIntOrNull() !in listedIds }
            ?.forEach(File::delete)

        isLoaded = true
        _state.value = HitsWidgetState(snapshot = snapshot)
    }

    /** Removes everything once the last widget is gone. */
    fun clear() {
        directory.deleteRecursively()
        _state.value = HitsWidgetState()
    }

    companion object {
        const val DIRECTORY_NAME = "hits_widget"
        private const val SNAPSHOT_FILE_NAME = "snapshot.json"
        private const val THUMBNAIL_SUFFIX = ".jpg"
    }
}
