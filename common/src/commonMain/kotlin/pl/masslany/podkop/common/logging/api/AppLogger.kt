package pl.masslany.podkop.common.logging.api

interface AppLogger {
    fun debug(message: String)

    fun info(message: String)

    fun warn(message: String, throwable: Throwable? = null)

    fun error(message: String, throwable: Throwable? = null)
}
