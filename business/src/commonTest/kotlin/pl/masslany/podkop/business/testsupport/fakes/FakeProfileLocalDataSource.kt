package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.profile.data.local.api.ProfileLocalDataSource
import pl.masslany.podkop.business.profile.domain.models.Profile
import pl.masslany.podkop.business.profile.domain.models.ProfileShort

class FakeProfileLocalDataSource : ProfileLocalDataSource {
    var cachedProfile: Profile? = null
    var cachedProfileShort: ProfileShort? = null
    var getProfileCalls = 0
    var getProfileShortCalls = 0
    val setProfileCalls = mutableListOf<Profile>()
    val setProfileShortCalls = mutableListOf<ProfileShort>()
    var clearProfileCalls = 0
    var clearProfileShortCalls = 0

    override suspend fun getProfile(): Profile? {
        getProfileCalls += 1
        return cachedProfile
    }

    override suspend fun setProfile(profile: Profile) {
        cachedProfile = profile
        setProfileCalls += profile
    }

    override suspend fun getProfileShort(): ProfileShort? {
        getProfileShortCalls += 1
        return cachedProfileShort
    }

    override suspend fun setProfileShort(profileShort: ProfileShort) {
        cachedProfileShort = profileShort
        setProfileShortCalls += profileShort
    }

    override suspend fun clearProfile() {
        cachedProfile = null
        clearProfileCalls += 1
    }

    override suspend fun clearProfileShort() {
        cachedProfileShort = null
        clearProfileShortCalls += 1
    }
}
