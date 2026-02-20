package pl.masslany.podkop.business.profile.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.data.main.mapper.common.toResources
import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.profile.data.api.ProfileDataSource
import pl.masslany.podkop.business.profile.data.main.mapper.toProfile
import pl.masslany.podkop.business.profile.data.main.mapper.toProfileShort
import pl.masslany.podkop.business.profile.data.main.mapper.toUsersAutoComplete
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.business.profile.domain.models.Profile
import pl.masslany.podkop.business.profile.domain.models.ProfileShort
import pl.masslany.podkop.business.profile.domain.models.UsersAutoComplete
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class ProfileRepositoryImpl(
    private val profileDataSource: ProfileDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : ProfileRepository {
    override suspend fun getProfileShort(): Result<ProfileShort> {
        return withContext(dispatcherProvider.io) {
            profileDataSource.getProfileShort().mapCatching {
                it.toProfileShort()
            }
        }
    }

    override suspend fun getProfile(): Result<Profile> {
        return withContext(dispatcherProvider.io) {
            profileDataSource.getProfile().mapCatching {
                it.toProfile()
            }
        }
    }

    override suspend fun getProfile(name: String): Result<Profile> {
        return withContext(dispatcherProvider.io) {
            profileDataSource.getProfile(name).mapCatching {
                it.toProfile()
            }
        }
    }

    override suspend fun getUsersAutoComplete(query: String): Result<UsersAutoComplete> {
        return withContext(dispatcherProvider.io) {
            profileDataSource.getUsersAutoComplete(query).mapCatching {
                it.toUsersAutoComplete()
            }
        }
    }

    override suspend fun getProfileActions(
        username: String,
        page: Int,
    ): Result<Resources> {
        return getProfileResources { profileDataSource.getProfileActions(username, page) }
    }

    override suspend fun getProfileEntriesAdded(
        username: String,
        page: Int,
    ): Result<Resources> {
        return getProfileResources { profileDataSource.getProfileEntriesAdded(username, page) }
    }

    override suspend fun getProfileEntriesVoted(
        username: String,
        page: Int,
    ): Result<Resources> {
        return getProfileResources { profileDataSource.getProfileEntriesVoted(username, page) }
    }

    override suspend fun getProfileEntriesCommented(
        username: String,
        page: Int,
    ): Result<Resources> {
        return getProfileResources { profileDataSource.getProfileEntriesCommented(username, page) }
    }

    override suspend fun getProfileLinksAdded(
        username: String,
        page: Int,
    ): Result<Resources> {
        return getProfileResources { profileDataSource.getProfileLinksAdded(username, page) }
    }

    override suspend fun getProfileLinksPublished(
        username: String,
        page: Int,
    ): Result<Resources> {
        return getProfileResources { profileDataSource.getProfileLinksPublished(username, page) }
    }

    override suspend fun getProfileLinksUp(
        username: String,
        page: Int,
    ): Result<Resources> {
        return getProfileResources { profileDataSource.getProfileLinksUp(username, page) }
    }

    override suspend fun getProfileLinksDown(
        username: String,
        page: Int,
    ): Result<Resources> {
        return getProfileResources { profileDataSource.getProfileLinksDown(username, page) }
    }

    override suspend fun getProfileLinksCommented(
        username: String,
        page: Int,
    ): Result<Resources> {
        return getProfileResources { profileDataSource.getProfileLinksCommented(username, page) }
    }

    override suspend fun getProfileLinksRelated(
        username: String,
        page: Int,
    ): Result<Resources> {
        return getProfileResources { profileDataSource.getProfileLinksRelated(username, page) }
    }

    override suspend fun getProfileObservedTags(
        username: String,
        page: Int,
    ): Result<Resources> {
        return getProfileResources { profileDataSource.getProfileObservedTags(username, page) }
    }

    override suspend fun getProfileObservedUsersFollowing(
        username: String,
        page: Int,
    ): Result<Resources> {
        return getProfileResources { profileDataSource.getProfileObservedUsersFollowing(username, page) }
    }

    override suspend fun getProfileObservedUsersFollowers(
        username: String,
        page: Int,
    ): Result<Resources> {
        return getProfileResources { profileDataSource.getProfileObservedUsersFollowers(username, page) }
    }

    private suspend fun getProfileResources(
        request: suspend () -> Result<ResourceResponseDto>,
    ): Result<Resources> {
        return withContext(dispatcherProvider.io) {
            request().mapCatching {
                it.toResources()
            }
        }
    }
}
