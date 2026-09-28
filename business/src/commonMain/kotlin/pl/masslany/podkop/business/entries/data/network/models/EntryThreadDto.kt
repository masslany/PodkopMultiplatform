package pl.masslany.podkop.business.entries.data.network.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import pl.masslany.podkop.business.common.data.network.models.common.ResourceItemDto

@Serializable
data class EntryThreadResponseDto(
    @SerialName("data")
    val data: EntryThreadNodeDto,
)

@Serializable
data class EntryThreadRepliesResponseDto(
    @SerialName("data")
    val data: EntryThreadRepliesDto,
)

/**
 * Direct children of a thread node. `count` is the number of direct children on the server,
 * `items` is only the loaded preview of them.
 */
@Serializable
data class EntryThreadRepliesDto(
    @SerialName("count")
    val count: Int = 0,
    @SerialName("items")
    val items: List<EntryThreadNodeDto> = emptyList(),
)

/**
 * A node of the `entries-threads` tree: an entry or entry comment plus its nested replies.
 *
 * The node's own fields are decoded as a regular [ResourceItemDto]. Its `comments` object is
 * decoded separately because it holds nested thread nodes, not the flat comment preview that
 * [ResourceItemDto.comments] expects.
 */
@Serializable(with = EntryThreadNodeDtoSerializer::class)
data class EntryThreadNodeDto(
    val item: ResourceItemDto,
    val nesting: Int?,
    val host: Boolean,
    val replies: EntryThreadRepliesDto?,
)

object EntryThreadNodeDtoSerializer : KSerializer<EntryThreadNodeDto> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("EntryThreadNodeDto")

    override fun deserialize(decoder: Decoder): EntryThreadNodeDto {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("EntryThreadNodeDto can only be decoded from JSON")
        val node = input.decodeJsonElement().jsonObject
        val replies = node[COMMENTS_KEY] as? JsonObject

        return EntryThreadNodeDto(
            item = input.json.decodeFromJsonElement(ResourceItemDto.serializer(), JsonObject(node - COMMENTS_KEY)),
            nesting = (node[NESTING_KEY] as? JsonPrimitive)?.intOrNull,
            host = (node[HOST_KEY] as? JsonPrimitive)?.booleanOrNull ?: false,
            replies = replies?.let { input.json.decodeFromJsonElement(EntryThreadRepliesDto.serializer(), it) },
        )
    }

    override fun serialize(encoder: Encoder, value: EntryThreadNodeDto) {
        val output = encoder as? JsonEncoder
            ?: throw SerializationException("EntryThreadNodeDto can only be encoded to JSON")
        val item = output.json.encodeToJsonElement(ResourceItemDto.serializer(), value.item).jsonObject
        val extras = buildMap {
            value.nesting?.let { put(NESTING_KEY, JsonPrimitive(it)) }
            put(HOST_KEY, JsonPrimitive(value.host))
            value.replies?.let {
                put(COMMENTS_KEY, output.json.encodeToJsonElement(EntryThreadRepliesDto.serializer(), it))
            }
        }
        output.encodeJsonElement(JsonObject(item + extras))
    }

    private const val COMMENTS_KEY = "comments"
    private const val NESTING_KEY = "nesting"
    private const val HOST_KEY = "host"
}
