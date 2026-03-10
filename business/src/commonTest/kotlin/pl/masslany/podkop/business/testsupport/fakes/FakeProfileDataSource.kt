package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.profile.data.api.ProfileDataSource
import pl.masslany.podkop.business.profile.data.network.models.ObservedTagsResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ObservedUsersResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileBadgesResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileNoteResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileShortDto
import pl.masslany.podkop.business.profile.data.network.models.UsersAutoCompleteResponseDto

class FakeProfileDataSource : ProfileDataSource {
    enum class ResourceEndpoint {
        Actions,
        EntriesAdded,
        EntriesVoted,
        EntriesCommented,
        LinksAdded,
        LinksPublished,
        LinksUp,
        LinksDown,
        LinksCommented,
        LinksRelated,
    }

    data class ResourceCall(
        val endpoint: ResourceEndpoint,
        val username: String,
        val page: Int,
    )

    data class ObservedUsersCall(
        val username: String,
        val page: Int,
    )

    data class UpdateProfileNoteCall(
        val username: String,
        val content: String,
    )

    var getProfileShortResult: Result<ProfileShortDto> = unstubbedResult("ProfileDataSource.getProfileShort")
    var getProfileResult: Result<ProfileDto> = unstubbedResult("ProfileDataSource.getProfile")
    var getProfileByNameResult: Result<ProfileDto> = unstubbedResult("ProfileDataSource.getProfile(name)")
    var getProfileBadgesResult: Result<ProfileBadgesResponseDto> =
        unstubbedResult("ProfileDataSource.getProfileBadges")
    var getProfileNoteResult: Result<ProfileNoteResponseDto> =
        unstubbedResult("ProfileDataSource.getProfileNote")
    var updateProfileNoteResult: Result<Unit> =
        unstubbedResult("ProfileDataSource.updateProfileNote")
    var observeUserResult: Result<Unit> = unstubbedResult("ProfileDataSource.observeUser")
    var unobserveUserResult: Result<Unit> = unstubbedResult("ProfileDataSource.unobserveUser")
    var getUsersAutoCompleteResult: Result<UsersAutoCompleteResponseDto> =
        unstubbedResult("ProfileDataSource.getUsersAutoComplete")
    var resourceResult: Result<ResourceResponseDto> = unstubbedResult("ProfileDataSource.profileResource")
    var observedTagsResult: Result<ObservedTagsResponseDto> = unstubbedResult("ProfileDataSource.getProfileObservedTags")
    var observedUsersFollowingResult: Result<ObservedUsersResponseDto> =
        unstubbedResult("ProfileDataSource.getProfileObservedUsersFollowing")
    var observedUsersFollowersResult: Result<ObservedUsersResponseDto> =
        unstubbedResult("ProfileDataSource.getProfileObservedUsersFollowers")

    var getProfileShortCalls = 0
    var getProfileCalls = 0
    val getProfileByNameCalls = mutableListOf<String>()
    val getProfileBadgesCalls = mutableListOf<String>()
    val getProfileNoteCalls = mutableListOf<String>()
    val updateProfileNoteCalls = mutableListOf<UpdateProfileNoteCall>()
    val observeUserCalls = mutableListOf<String>()
    val unobserveUserCalls = mutableListOf<String>()
    val getUsersAutoCompleteCalls = mutableListOf<String>()
    val resourceCalls = mutableListOf<ResourceCall>()
    val getProfileObservedTagsCalls = mutableListOf<Pair<String, Int>>()
    val getProfileObservedUsersFollowingCalls = mutableListOf<ObservedUsersCall>()
    val getProfileObservedUsersFollowersCalls = mutableListOf<ObservedUsersCall>()

    override suspend fun getProfileShort(): Result<ProfileShortDto> {
        getProfileShortCalls += 1
        return getProfileShortResult
    }

    override suspend fun getProfile(): Result<ProfileDto> {
        getProfileCalls += 1
        return getProfileResult
    }

    override suspend fun getProfile(name: String): Result<ProfileDto> {
        getProfileByNameCalls += name
        return getProfileByNameResult
    }

    override suspend fun getProfileBadges(username: String): Result<ProfileBadgesResponseDto> {
        getProfileBadgesCalls += username
        return getProfileBadgesResult
    }

    override suspend fun getProfileNote(username: String): Result<ProfileNoteResponseDto> {
        getProfileNoteCalls += username
        return getProfileNoteResult
    }

    override suspend fun updateProfileNote(
        username: String,
        content: String,
    ): Result<Unit> {
        updateProfileNoteCalls += UpdateProfileNoteCall(username = username, content = content)
        return updateProfileNoteResult
    }

    override suspend fun observeUser(username: String): Result<Unit> {
        observeUserCalls += username
        return observeUserResult
    }

    override suspend fun unobserveUser(username: String): Result<Unit> {
        unobserveUserCalls += username
        return unobserveUserResult
    }

    override suspend fun getUsersAutoComplete(query: String): Result<UsersAutoCompleteResponseDto> {
        getUsersAutoCompleteCalls += query
        return getUsersAutoCompleteResult
    }

    override suspend fun getProfileActions(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> = profileResource(ResourceEndpoint.Actions, username, page)

    override suspend fun getProfileEntriesAdded(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> = profileResource(ResourceEndpoint.EntriesAdded, username, page)

    override suspend fun getProfileEntriesVoted(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> = profileResource(ResourceEndpoint.EntriesVoted, username, page)

    override suspend fun getProfileEntriesCommented(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> = profileResource(ResourceEndpoint.EntriesCommented, username, page)

    override suspend fun getProfileLinksAdded(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> = profileResource(ResourceEndpoint.LinksAdded, username, page)

    override suspend fun getProfileLinksPublished(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> = profileResource(ResourceEndpoint.LinksPublished, username, page)

    override suspend fun getProfileLinksUp(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> = profileResource(ResourceEndpoint.LinksUp, username, page)

    override suspend fun getProfileLinksDown(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> = profileResource(ResourceEndpoint.LinksDown, username, page)

    override suspend fun getProfileLinksCommented(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> = profileResource(ResourceEndpoint.LinksCommented, username, page)

    override suspend fun getProfileLinksRelated(
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> = profileResource(ResourceEndpoint.LinksRelated, username, page)

    override suspend fun getProfileObservedTags(
        username: String,
        page: Int,
    ): Result<ObservedTagsResponseDto> {
        getProfileObservedTagsCalls += username to page
        return observedTagsResult
    }

    override suspend fun getProfileObservedUsersFollowing(
        username: String,
        page: Int,
    ): Result<ObservedUsersResponseDto> {
        getProfileObservedUsersFollowingCalls += ObservedUsersCall(username, page)
        return observedUsersFollowingResult
    }

    override suspend fun getProfileObservedUsersFollowers(
        username: String,
        page: Int,
    ): Result<ObservedUsersResponseDto> {
        getProfileObservedUsersFollowersCalls += ObservedUsersCall(username, page)
        return observedUsersFollowersResult
    }

    private fun profileResource(
        endpoint: ResourceEndpoint,
        username: String,
        page: Int,
    ): Result<ResourceResponseDto> {
        resourceCalls += ResourceCall(endpoint = endpoint, username = username, page = page)
        return resourceResult
    }
}
