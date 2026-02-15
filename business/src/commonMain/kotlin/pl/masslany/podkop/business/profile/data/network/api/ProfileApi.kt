package pl.masslany.podkop.business.profile.data.network.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileShortDto
import pl.masslany.podkop.business.profile.data.network.models.UsersAutoCompleteResponseDto

interface ProfileApi {
    suspend fun getProfileShort(): Result<ProfileShortDto>

    suspend fun getProfile(): Result<ProfileDto>

    suspend fun getProfile(name: String): Result<ProfileDto>

    suspend fun getUsersAutoComplete(query: String): Result<UsersAutoCompleteResponseDto>

    suspend fun getProfileActions(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto>
}
