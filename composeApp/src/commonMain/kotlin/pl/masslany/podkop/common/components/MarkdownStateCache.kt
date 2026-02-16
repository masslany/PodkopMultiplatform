package pl.masslany.podkop.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.mikepenz.markdown.model.ReferenceLinkHandlerImpl
import com.mikepenz.markdown.model.State
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

private const val MAX_CACHE_SIZE = 300

/**
 * Simple in-memory LRU cache for parsed markdown states.
 * It helps avoid re-parsing when list items are revisited during scrolling.
 */
class MarkdownStateCache(private val maxCacheSize: Int = MAX_CACHE_SIZE) {
    private val cache = linkedMapOf<String, State.Success>()
    private val lock = Mutex()
    private val parser = MarkdownParser(GFMFlavourDescriptor())

    suspend fun get(content: String): State.Success? = lock.withLock {
        cache[content]
    }

    suspend fun getOrParse(content: String): State.Success {
        get(content)?.let { return it }

        val parsed = State.Success(
            node = parser.buildMarkdownTreeFromString(content),
            content = content,
            linksLookedUp = false,
            referenceLinkHandler = ReferenceLinkHandlerImpl(),
        )

        lock.withLock {
            cache[content] = parsed
            if (cache.size > maxCacheSize) {
                val eldestKey = cache.keys.firstOrNull()
                if (eldestKey != null) {
                    cache.remove(eldestKey)
                }
            }
        }
        return parsed
    }
}

val LocalMarkdownStateCache = staticCompositionLocalOf { MarkdownStateCache() }

@Composable
fun rememberMarkdownStateCache(
    maxCacheSize: Int = MAX_CACHE_SIZE,
): MarkdownStateCache = remember(maxCacheSize) {
    MarkdownStateCache(maxCacheSize = maxCacheSize)
}
