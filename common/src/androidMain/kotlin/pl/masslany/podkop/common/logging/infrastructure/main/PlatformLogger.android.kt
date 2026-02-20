package pl.masslany.podkop.common.logging.infrastructure.main

import pl.masslany.podkop.common.logging.api.AppLogger
import timber.log.Timber

actual class PlatformLogger actual constructor() : AppLogger {
    override fun debug(message: String) {
        ensureTree()
        Timber.d(message)
    }

    override fun info(message: String) {
        ensureTree()
        Timber.i(message)
    }

    override fun warn(message: String, throwable: Throwable?) {
        ensureTree()
        if (throwable == null) {
            Timber.w(message)
        } else {
            Timber.w(throwable, message)
        }
    }

    override fun error(message: String, throwable: Throwable?) {
        ensureTree()
        if (throwable == null) {
            Timber.e(message)
        } else {
            Timber.e(throwable, message)
        }
    }

    private fun ensureTree() {
        if (Timber.treeCount == 0) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
