package pl.masslany.podkop.business.profile.data.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ObservedTagsResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ObservedUsersResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileShortDto
import pl.masslany.podkop.business.profile.data.network.models.UsersAutoCompleteResponseDto

interface ProfileDataSource {
    suspend fun getProfileShort(): Result<ProfileShortDto>

    suspend fun getProfile(): Result<ProfileDto>

    suspend fun getProfile(name: String): Result<ProfileDto>

    suspend fun observeUser(username: String): Result<Unit>

    suspend fun unobserveUser(username: String): Result<Unit>

    suspend fun getUsersAutoComplete(query: String): Result<UsersAutoCompleteResponseDto>

    suspend fun getProfileActions(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto>

    suspend fun getProfileEntriesAdded(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto>

    suspend fun getProfileEntriesVoted(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto>

    suspend fun getProfileEntriesCommented(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto>

    suspend fun getProfileLinksAdded(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto>

    suspend fun getProfileLinksPublished(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto>

    suspend fun getProfileLinksUp(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto>

    suspend fun getProfileLinksDown(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto>

    suspend fun getProfileLinksCommented(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto>

    suspend fun getProfileLinksRelated(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto>

    suspend fun getProfileObservedTags(
        username: String,
        page: Int,
    ): Result<ObservedTagsResponseDto>

    suspend fun getProfileObservedUsersFollowing(
        username: String,
        page: Int,
    ): Result<ObservedUsersResponseDto>

    suspend fun getProfileObservedUsersFollowers(
        username: String,
        page: Int,
    ): Result<ObservedUsersResponseDto>
}
