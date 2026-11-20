package pl.masslany.podkop.common.platform

import android.app.Application
import java.io.File

class AppMaintenanceController(private val application: Application) {
    suspend fun clearCache(): Boolean = runCatching {
        val internalCacheCleared = application.cacheDir.clearContents()
        val externalCacheCleared = application.externalCacheDir?.clearContents() ?: true
        internalCacheCleared && externalCacheCleared
    }.getOrDefault(false)
}

private fun File.clearContents(): Boolean {
    if (!exists()) {
        return true
    }

    val children = listFiles() ?: return isDirectory
    return children.all { child -> child.deleteRecursively() }
}
