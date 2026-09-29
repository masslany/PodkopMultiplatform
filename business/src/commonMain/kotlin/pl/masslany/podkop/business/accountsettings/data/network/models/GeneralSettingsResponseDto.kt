package pl.masslany.podkop.business.accountsettings.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeneralSettingsResponseDto(
    @SerialName("data")
    val data: GeneralSettingsDto,
)

@Serializable
data class GeneralSettingsDto(
    @SerialName("show18")
    val show18: Boolean = false,
)
