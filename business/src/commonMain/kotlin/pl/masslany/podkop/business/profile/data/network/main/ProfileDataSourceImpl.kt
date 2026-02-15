package pl.masslany.podkop.business.profile.data.network.main

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.profile.data.api.ProfileDataSource
import pl.masslany.podkop.business.profile.data.network.api.ProfileApi
import pl.masslany.podkop.business.profile.data.network.models.ProfileDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileShortDto
import pl.masslany.podkop.business.profile.data.network.models.UsersAutoCompleteResponseDto

class ProfileDataSourceImpl(
    private val profileApi: ProfileApi,
) : ProfileDataSource {
    override suspend fun getProfileShort(): Result<ProfileShortDto> {
        return profileApi.getProfileShort()
    }

    override suspend fun getProfile(): Result<ProfileDto> {
        return profileApi.getProfile()
    }

    override suspend fun getProfile(name: String): Result<ProfileDto> {
        return profileApi.getProfile(name)
    }

    override suspend fun getUsersAutoComplete(query: String): Result<UsersAutoCompleteResponseDto> {
        return profileApi.getUsersAutoComplete(query)
    }

    override suspend fun getProfileActions(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return profileApi.getProfileActions(username, page)
    }
}
