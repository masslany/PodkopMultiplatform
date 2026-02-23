package pl.masslany.podkop.common.network.models.request

import kotlin.jvm.JvmField

const val REQUEST_HEADER_SKIP_AUTH = "X-Podkop-Skip-Auth"

data class Request<T>(
    val method: HttpMethod,
    val path: String,
    val headers: Map<String, String>? = null,
    val queryParameters: Map<String, String>? = null,
    @JvmField
    var body: Any? = null,
) {
    enum class HttpMethod {
        GET,
        DELETE,
        PATCH,
        POST,
        PUT,
    }
}
