package pl.masslany.podkop.widget

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

class HitsWidgetStoreTest {
    private val directory: File = Files.createTempDirectory("hits-widget").toFile()

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun `a saved snapshot survives a new process`() = runBlocking {
        val snapshot = snapshot(1, 2)
        HitsWidgetStore(directory).save(snapshot, thumbnails = mapOf(1 to byteArrayOf(1, 2, 3)))

        val restored = HitsWidgetStore(directory)

        assertEquals(snapshot, restored.load().snapshot)
        assertContentEquals(byteArrayOf(1, 2, 3), restored.thumbnailFile(1).readBytes())
    }

    @Test
    fun `saving drops thumbnails of links that left the list`() = runBlocking {
        val store = HitsWidgetStore(directory)
        store.save(snapshot(1, 2), thumbnails = mapOf(1 to byteArrayOf(1), 2 to byteArrayOf(2)))

        store.save(snapshot(2, 3), thumbnails = mapOf(3 to byteArrayOf(3)))

        assertFalse(store.thumbnailFile(1).exists())
        assertTrue(store.thumbnailFile(2).exists())
        assertTrue(store.thumbnailFile(3).exists())
    }

    @Test
    fun `a failed refresh keeps the last snapshot`() = runBlocking {
        val store = HitsWidgetStore(directory)
        store.save(snapshot(1), thumbnails = emptyMap())
        store.setRefreshing(true)

        store.markRefreshFailed()

        assertEquals(HitsWidgetState(snapshot = snapshot(1), lastRefreshFailed = true), store.state.value)
    }

    @Test
    fun `an unreadable snapshot loads as no data`() = runBlocking {
        File(directory, "snapshot.json").writeText("{not json")

        assertNull(HitsWidgetStore(directory).load().snapshot)
    }

    @Test
    fun `clear removes the files and the snapshot`() = runBlocking {
        val store = HitsWidgetStore(directory)
        store.save(snapshot(1), thumbnails = mapOf(1 to byteArrayOf(1)))

        store.clear()

        assertFalse(directory.exists())
        assertEquals(HitsWidgetState(), store.state.value)
    }

    private fun snapshot(vararg ids: Int) = HitsWidgetSnapshot(
        updatedAtMillis = 1_000,
        items = ids.map { HitsWidgetItem(id = it, title = "Link $it", votes = it, comments = 0) },
    )
}
