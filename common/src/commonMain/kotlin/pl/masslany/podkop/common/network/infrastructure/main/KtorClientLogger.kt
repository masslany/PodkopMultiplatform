package pl.masslany.podkop.common.network.infrastructure.main

import io.ktor.client.plugins.logging.Logger
import pl.masslany.podkop.common.logging.api.AppLogger

internal class KtorClientLogger(
    private val appLogger: AppLogger,
) : Logger {
    override fun log(message: String) {
        message.chunked(MAX_CHUNK_LENGTH).forEach { chunk ->
            appLogger.debug("HTTP: $chunk")
        }
    }

    private companion object {
        const val MAX_CHUNK_LENGTH = 4095
    }
}
