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

    override suspend fun getProfileEntriesAdded(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return profileApi.getProfileEntriesAdded(username, page)
    }

    override suspend fun getProfileEntriesVoted(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return profileApi.getProfileEntriesVoted(username, page)
    }

    override suspend fun getProfileEntriesCommented(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return profileApi.getProfileEntriesCommented(username, page)
    }

    override suspend fun getProfileLinksAdded(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return profileApi.getProfileLinksAdded(username, page)
    }

    override suspend fun getProfileLinksPublished(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return profileApi.getProfileLinksPublished(username, page)
    }

    override suspend fun getProfileLinksUp(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return profileApi.getProfileLinksUp(username, page)
    }

    override suspend fun getProfileLinksDown(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return profileApi.getProfileLinksDown(username, page)
    }

    override suspend fun getProfileLinksCommented(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return profileApi.getProfileLinksCommented(username, page)
    }

    override suspend fun getProfileLinksRelated(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return profileApi.getProfileLinksRelated(username, page)
    }

    override suspend fun getProfileObservedTags(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return profileApi.getProfileObservedTags(username, page)
    }

    override suspend fun getProfileObservedUsersFollowing(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return profileApi.getProfileObservedUsersFollowing(username, page)
    }

    override suspend fun getProfileObservedUsersFollowers(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        return profileApi.getProfileObservedUsersFollowers(username, page)
    }
}
