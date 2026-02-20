package pl.masslany.podkop.common.logging.infrastructure.main

import pl.masslany.podkop.common.logging.api.AppLogger
import platform.Foundation.NSLog

actual class PlatformLogger actual constructor() : AppLogger {
    override fun debug(message: String) {
        log(level = "DEBUG", message = message)
    }

    override fun info(message: String) {
        log(level = "INFO", message = message)
    }

    override fun warn(message: String, throwable: Throwable?) {
        log(level = "WARN", message = message, throwable = throwable)
    }

    override fun error(message: String, throwable: Throwable?) {
        log(level = "ERROR", message = message, throwable = throwable)
    }

    private fun log(
        level: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        val errorPart = throwable?.let { "\n${it.stackTraceToString()}" }.orEmpty()
        NSLog("[%@] %@%@", level, message, errorPart)
    }
}
