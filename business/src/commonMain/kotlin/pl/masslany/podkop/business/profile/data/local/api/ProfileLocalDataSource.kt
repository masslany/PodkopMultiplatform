package pl.masslany.podkop.business.profile.data.local.api

import pl.masslany.podkop.business.profile.domain.models.Profile
import pl.masslany.podkop.business.profile.domain.models.ProfileShort

interface ProfileLocalDataSource {
    suspend fun getProfile(): Profile?

    suspend fun setProfile(profile: Profile)

    suspend fun getProfileShort(): ProfileShort?

    suspend fun setProfileShort(profileShort: ProfileShort)

    suspend fun clearProfile()

    suspend fun clearProfileShort()
}
