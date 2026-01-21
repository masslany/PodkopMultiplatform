package pl.masslany.podkop.common.network.infrastructure.main

import io.ktor.client.plugins.logging.Logger

class CommonLogger : Logger {
    override fun log(message: String) {
        message.chunked(4095).forEach { chunkedMessage ->
            println("CommonLogger: $chunkedMessage")
        }
    }
}
