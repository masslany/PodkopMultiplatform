package pl.masslany.podkop.common.network.infrastructure.main

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

@OptIn(ExperimentalEncodingApi::class, ExperimentalTime::class)
fun String.isExpiringIn(leewaySeconds: Long, json: Json): Boolean {
    val payloadPart = split('.').getOrNull(1) ?: return true
    val normalizedPayload = payloadPart.replace('-', '+').replace('_', '/').padBase64()
    val payloadJson = runCatching { Base64.decode(normalizedPayload).decodeToString() }.getOrNull() ?: return true
    val expirationSeconds =
        runCatching {
            json.parseToJsonElement(payloadJson).jsonObject["exp"]?.jsonPrimitive?.longOrNull
        }.getOrNull() ?: return true

    val nowSeconds = Clock.System.now().epochSeconds
    return (expirationSeconds - nowSeconds) <= leewaySeconds
}

private fun String.padBase64(): String {
    val padLen = (4 - (length % 4)) % 4
    if (padLen == 0) {
        return this
    }
    return this + "=".repeat(padLen)
}
