package pl.masslany.podkop.business.observed.data.network.models

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer

object ObservedItemsSerializer :
    KSerializer<List<ObservedItemDto>> by ListSerializer(ObservedItemDto.serializer())
