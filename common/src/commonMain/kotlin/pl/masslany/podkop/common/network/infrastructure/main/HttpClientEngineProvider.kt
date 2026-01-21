package pl.masslany.podkop.common.network.infrastructure.main

import io.ktor.client.engine.HttpClientEngine

expect object HttpClientEngineProvider {
    fun provide(): HttpClientEngine
}