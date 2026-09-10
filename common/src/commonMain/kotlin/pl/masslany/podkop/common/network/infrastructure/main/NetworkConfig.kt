package pl.masslany.podkop.common.network.infrastructure.main

data class NetworkConfig(
    val baseUrl: String = DEFAULT_BASE_URL,
) {
    val normalizedBaseUrl: String =
        if (baseUrl.endsWith("/")) {
            baseUrl
        } else {
            "$baseUrl/"
        }

    companion object {
        const val DEFAULT_BASE_URL = "https://wykop.pl/"
    }
}
