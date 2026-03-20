package pl.masslany.podkop.business.entries.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EntrySurveyVoteRequestDto(
    @SerialName("data")
    val data: EntrySurveyVoteDataDto,
)

@Serializable
data class EntrySurveyVoteDataDto(
    @SerialName("vote")
    val vote: Int,
)
