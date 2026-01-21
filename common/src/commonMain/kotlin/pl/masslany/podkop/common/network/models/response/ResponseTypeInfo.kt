package pl.masslany.podkop.common.network.models.response

import kotlin.reflect.KClass
import kotlin.reflect.KType

data class ResponseTypeInfo(
    val type: KClass<*>,
    val kotlinType: KType? = null,
)
