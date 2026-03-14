package pl.masslany.podkop

import android.app.Application
import java.io.File
import pl.masslany.podkop.common.platform.AppMaintenanceController

class AndroidAppMaintenanceController(
    private val application: Application,
) : AppMaintenanceController {
    override val supportsCacheClearing: Boolean = true

    override suspend fun clearCache(): Boolean = runCatching {
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
