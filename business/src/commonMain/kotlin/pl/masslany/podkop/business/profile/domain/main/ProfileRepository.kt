package pl.masslany.podkop.business.profile.domain.main

import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.profile.domain.models.ObservedTags
import pl.masslany.podkop.business.profile.domain.models.ObservedUsers
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

    suspend fun getProfileEntriesAdded(
        username: String,
        page: Int,
    ): Result<Resources>

    suspend fun getProfileEntriesVoted(
        username: String,
        page: Int,
    ): Result<Resources>

    suspend fun getProfileEntriesCommented(
        username: String,
        page: Int,
    ): Result<Resources>

    suspend fun getProfileLinksAdded(
        username: String,
        page: Int,
    ): Result<Resources>

    suspend fun getProfileLinksPublished(
        username: String,
        page: Int,
    ): Result<Resources>

    suspend fun getProfileLinksUp(
        username: String,
        page: Int,
    ): Result<Resources>

    suspend fun getProfileLinksDown(
        username: String,
        page: Int,
    ): Result<Resources>

    suspend fun getProfileLinksCommented(
        username: String,
        page: Int,
    ): Result<Resources>

    suspend fun getProfileLinksRelated(
        username: String,
        page: Int,
    ): Result<Resources>

    suspend fun getProfileObservedTags(
        username: String,
        page: Int,
    ): Result<ObservedTags>

    suspend fun getProfileObservedUsersFollowing(
        username: String,
        page: Int,
    ): Result<ObservedUsers>

    suspend fun getProfileObservedUsersFollowers(
        username: String,
        page: Int,
    ): Result<ObservedUsers>
}
