package pl.masslany.podkop.testsupport.fakes

import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.business.profile.domain.models.ObservedTags
import pl.masslany.podkop.business.profile.domain.models.ObservedUsers
import pl.masslany.podkop.business.profile.domain.models.Profile
import pl.masslany.podkop.business.profile.domain.models.ProfileBadge
import pl.masslany.podkop.business.profile.domain.models.ProfileNote
import pl.masslany.podkop.business.profile.domain.models.ProfileShort
import pl.masslany.podkop.business.profile.domain.models.UsersAutoComplete

class FakeProfileRepository : ProfileRepository {
    var getUsersAutoCompleteHandler: suspend (query: String) -> Result<UsersAutoComplete> =
        { Result.success(UsersAutoComplete(users = emptyList())) }

    val getUsersAutoCompleteCalls = mutableListOf<String>()

    override suspend fun getUsersAutoComplete(query: String): Result<UsersAutoComplete> {
        getUsersAutoCompleteCalls += query
        return getUsersAutoCompleteHandler(query)
    }

    override suspend fun getProfileShort(): Result<ProfileShort> = notUsed()
    override suspend fun getProfile(): Result<Profile> = notUsed()
    override suspend fun getProfile(name: String): Result<Profile> = notUsed()
    override suspend fun getProfileBadges(username: String): Result<List<ProfileBadge>> = notUsed()
    override suspend fun getProfileNote(username: String): Result<ProfileNote> = notUsed()
    override suspend fun updateProfileNote(username: String, content: String): Result<Unit> = notUsed()
    override suspend fun observeUser(username: String): Result<Unit> = notUsed()
    override suspend fun unobserveUser(username: String): Result<Unit> = notUsed()
    override suspend fun getProfileActions(username: String, page: Int): Result<Resources> = notUsed()
    override suspend fun getProfileEntriesAdded(username: String, page: Int): Result<Resources> = notUsed()
    override suspend fun getProfileEntriesVoted(username: String, page: Int): Result<Resources> = notUsed()
    override suspend fun getProfileEntriesCommented(username: String, page: Int): Result<Resources> = notUsed()
    override suspend fun getProfileLinksAdded(username: String, page: Int): Result<Resources> = notUsed()
    override suspend fun getProfileLinksPublished(username: String, page: Int): Result<Resources> = notUsed()
    override suspend fun getProfileLinksUp(username: String, page: Int): Result<Resources> = notUsed()
    override suspend fun getProfileLinksDown(username: String, page: Int): Result<Resources> = notUsed()
    override suspend fun getProfileLinksCommented(username: String, page: Int): Result<Resources> = notUsed()
    override suspend fun getProfileLinksRelated(username: String, page: Int): Result<Resources> = notUsed()
    override suspend fun getProfileObservedTags(username: String, page: Int): Result<ObservedTags> = notUsed()
    override suspend fun getProfileObservedUsersFollowing(username: String, page: Int): Result<ObservedUsers> =
        notUsed()

    override suspend fun getProfileObservedUsersFollowers(username: String, page: Int): Result<ObservedUsers> =
        notUsed()
}
