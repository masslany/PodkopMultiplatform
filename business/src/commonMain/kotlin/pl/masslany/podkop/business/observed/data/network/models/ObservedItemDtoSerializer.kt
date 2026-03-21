package pl.masslany.podkop.business.observed.data.network.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.decodeFromJsonElement
import pl.masslany.podkop.business.common.data.network.models.common.ResourceItemDto

/**
 * Normalizes two observed item response shapes into one DTO.
 *
 * Most observed endpoints return plain resource objects, while
 * `observed/discussions` wraps them in `{ type, object, new_content_count }`.
 * This serializer decodes both formats into a single `ObservedItemDto`.
 */
object ObservedItemDtoSerializer : KSerializer<ObservedItemDto> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ObservedItemDto") {
        element("type", String.serializer().descriptor, isOptional = true)
        element("item", ResourceItemDto.serializer().descriptor)
        element("newContentCount", Int.serializer().descriptor, isOptional = true)
    }

    override fun deserialize(decoder: Decoder): ObservedItemDto {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("Observed items can be decoded only from JSON")
        val element = jsonDecoder.decodeJsonElement()
        val json = jsonDecoder.json

        return runCatching {
            json.decodeFromJsonElement<ObservedWrappedItemDto>(element)
        }.map { wrappedItem ->
            ObservedItemDto(
                type = wrappedItem.type,
                item = wrappedItem.item.copy(
                    resource = wrappedItem.item.resource ?: wrappedItem.type,
                ),
                newContentCount = wrappedItem.newContentCount,
            )
        }.getOrElse {
            val directItem = json.decodeFromJsonElement<ResourceItemDto>(element)
            ObservedItemDto(
                type = directItem.resource,
                item = directItem,
            )
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: ObservedItemDto,
    ) {
        val jsonEncoder = encoder as? kotlinx.serialization.json.JsonEncoder
            ?: throw SerializationException("Observed items can be encoded only to JSON")
        val element = if (value.newContentCount != null) {
            jsonEncoder.json.encodeToJsonElement(
                ObservedWrappedItemDto.serializer(),
                ObservedWrappedItemDto(
                    type = value.type ?: value.item.resource.orEmpty(),
                    item = value.item,
                    newContentCount = value.newContentCount,
                ),
            )
        } else {
            jsonEncoder.json.encodeToJsonElement(
                ResourceItemDto.serializer(),
                value.item,
            )
        }
        jsonEncoder.encodeJsonElement(element)
    }
}
