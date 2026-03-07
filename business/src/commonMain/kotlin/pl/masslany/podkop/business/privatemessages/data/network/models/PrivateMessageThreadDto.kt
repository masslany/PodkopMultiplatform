package pl.masslany.podkop.business.privatemessages.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto

@Serializable
data class PrivateMessageThreadDto(
    @SerialName("data")
    @Serializable(with = PrivateMessageThreadDataSerializer::class)
    val data: PrivateMessageThreadDataDto = PrivateMessageThreadDataDto(),
    @SerialName("pagination")
    val pagination: PaginationDto? = null,
)

@Serializable
data class PrivateMessageThreadDataDto(
    @SerialName("user")
    val user: PrivateMessageUserDto? = null,
    @SerialName("messages")
    val messages: List<PrivateMessageDto> = emptyList(),
)

object PrivateMessageThreadDataSerializer :
    JsonTransformingSerializer<PrivateMessageThreadDataDto>(PrivateMessageThreadDataDto.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonPrimitive && element.booleanOrNull == false) {
            return buildJsonObject { }
        }

        return element
    }
}
