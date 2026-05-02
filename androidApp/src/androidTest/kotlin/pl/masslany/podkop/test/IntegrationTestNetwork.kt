package pl.masslany.podkop.test

import pl.masslany.podkop.common.network.infrastructure.main.NetworkConfig

object IntegrationTestNetwork {
    @Volatile
    var baseUrl: String = NetworkConfig.DEFAULT_BASE_URL

    fun reset() {
        baseUrl = NetworkConfig.DEFAULT_BASE_URL
    }
}
