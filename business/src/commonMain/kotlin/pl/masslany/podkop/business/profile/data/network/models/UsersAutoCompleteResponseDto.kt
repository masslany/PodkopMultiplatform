package pl.masslany.podkop.business.profile.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsersAutoCompleteResponseDto(
    @SerialName("data")
    val data: List<UsersAutoCompleteDataDto>,
)
