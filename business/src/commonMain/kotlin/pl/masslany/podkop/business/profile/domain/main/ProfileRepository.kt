package pl.masslany.podkop.business.profile.domain.main

import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.profile.domain.models.Profile
import pl.masslany.podkop.business.profile.domain.models.ProfileShort
import pl.masslany.podkop.business.profile.domain.models.UsersAutoComplete

interface ProfileRepository {
    suspend fun getProfileShort(): Result<ProfileShort>

    suspend fun getProfile(): Result<Profile>

    suspend fun getProfile(name: String): Result<Profile>

    suspend fun getUsersAutoComplete(query: String): Result<UsersAutoComplete>

    suspend fun getProfileActions(
        username: String,
        page: Int,
    ): Result<Resources>
}
