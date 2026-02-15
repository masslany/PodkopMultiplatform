package pl.masslany.podkop.business.profile.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.common.data.main.mapper.common.toResources
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
        return withContext(dispatcherProvider.io) {
            profileDataSource.getProfileActions(username, page).mapCatching {
                it.toResources()
            }
        }
    }
}
