package pl.masslany.podkop.business.common.data.network.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Handles inconsistent backend payloads for `deleted`.
 *
 * Most endpoints return a semantic string like `moderator` or `author`, but
 * some observed/discussion payloads send `false` or `null` for the same field.
 * We normalize those variants into `String?` so the rest of the mapping layer
 * can keep treating "not deleted" as `null`.
 */
@OptIn(ExperimentalSerializationApi::class)
object FlexibleDeletedSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleDeleted", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: String?,
    ) {
        val jsonEncoder = encoder as? JsonEncoder
        if (jsonEncoder != null) {
            jsonEncoder.encodeJsonElement(value?.let(::JsonPrimitive) ?: JsonNull)
            return
        }

        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(value)
        }
    }

    override fun deserialize(decoder: Decoder): String? {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            return when (val element = jsonDecoder.decodeJsonElement()) {
                JsonNull -> null
                is JsonPrimitive -> if (element.isString) element.content else null
                else -> null
            }
        }

        return runCatching { decoder.decodeString() }.getOrNull()
    }
}
