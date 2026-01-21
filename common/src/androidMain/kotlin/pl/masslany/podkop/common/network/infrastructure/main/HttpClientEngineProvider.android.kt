package pl.masslany.podkop.common.network.infrastructure.main

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual object HttpClientEngineProvider {
    actual fun provide(): HttpClientEngine {
        return OkHttp.create()
    }
}
