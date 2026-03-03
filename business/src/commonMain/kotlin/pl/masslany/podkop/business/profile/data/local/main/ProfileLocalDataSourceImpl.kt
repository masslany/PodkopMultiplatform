package pl.masslany.podkop.business.profile.data.local.main

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pl.masslany.podkop.business.profile.data.local.api.ProfileLocalDataSource
import pl.masslany.podkop.business.profile.domain.models.Profile
import pl.masslany.podkop.business.profile.domain.models.ProfileShort

class ProfileLocalDataSourceImpl : ProfileLocalDataSource {
    private val profileMutex = Mutex()
    private var profile: Profile? = null
    private var profileShort: ProfileShort? = null

    override suspend fun getProfile(): Profile? {
        return profileMutex.withLock {
            profile
        }
    }

    override suspend fun setProfile(profile: Profile) {
        profileMutex.withLock {
            this.profile = profile
        }
    }

    override suspend fun getProfileShort(): ProfileShort? {
        return profileMutex.withLock {
            profileShort
        }
    }

    override suspend fun setProfileShort(profileShort: ProfileShort) {
        profileMutex.withLock {
            this.profileShort = profileShort
        }
    }

    override suspend fun clearProfile() {
        profileMutex.withLock {
            profile = null
        }
    }

    override suspend fun clearProfileShort() {
        profileMutex.withLock {
            profileShort = null
        }
    }
}
