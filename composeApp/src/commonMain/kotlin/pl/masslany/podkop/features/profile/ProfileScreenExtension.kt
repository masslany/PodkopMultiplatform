package pl.masslany.podkop.features.profile

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.profile.domain.main.ProfileRepository
import pl.masslany.podkop.business.profile.domain.models.ObservedTag
import pl.masslany.podkop.business.profile.domain.models.ObservedTags
import pl.masslany.podkop.business.profile.domain.models.ObservedUser
import pl.masslany.podkop.business.profile.domain.models.ObservedUsers
import pl.masslany.podkop.business.profile.domain.models.Profile
import pl.masslany.podkop.business.profile.domain.models.Summary
import pl.masslany.podkop.common.extensions.toMemberSinceState
import pl.masslany.podkop.common.models.UserItemState
import pl.masslany.podkop.common.models.avatar.toGenderIndicatorType
import pl.masslany.podkop.common.models.toNameColorType
import pl.masslany.podkop.features.profile.models.ProfileHeaderState
import pl.masslany.podkop.features.profile.models.ProfileListItem
import pl.masslany.podkop.features.profile.models.ProfileListItemsPage
import pl.masslany.podkop.features.profile.models.ProfileObservedTagItemState
import pl.masslany.podkop.features.profile.models.ProfileSubActionType
import pl.masslany.podkop.features.profile.models.ProfileSummaryItem

internal fun Profile.toProfileHeaderState(
    isLoggedIn: Boolean,
    viewerUsername: String?,
): ProfileHeaderState {
    val isOwnProfile = viewerUsername?.equals(name, ignoreCase = true) == true
    val observationEnabled = isLoggedIn && !isOwnProfile && canManageObservation
    val privateMessageEnabled = isLoggedIn && !isOwnProfile && (viewerUsername != null || observationEnabled)

    return ProfileHeaderState(
        username = name,
        avatarUrl = avatarUrl,
        rankPosition = rankPosition,
        backgroundUrl = backgroundUrl,
        genderIndicatorType = gender.toGenderIndicatorType(),
        nameColorType = color.toNameColorType(),
        memberSinceState = memberSince.toMemberSinceState(),
        isLoggedIn = isLoggedIn,
        isOwnProfile = isOwnProfile,
        isObserved = isObserved,
        isBlacklisted = isBlacklisted,
        canManageObservation = observationEnabled,
        canSendPrivateMessage = privateMessageEnabled,
    )
}

internal fun Summary.toSummaryItems(): ImmutableList<ProfileSummaryItem> =
    persistentListOf(
        ProfileSummaryItem.Actions(actions),
        ProfileSummaryItem.Links(links),
        ProfileSummaryItem.Entries(entries),
        ProfileSummaryItem.Followers(followers),
        ProfileSummaryItem.Following(followingTags + followingUsers),
    )

internal suspend fun ProfileRepository.getProfileListItems(
    username: String,
    subActionType: ProfileSubActionType,
    page: Int,
): Result<ProfileListItemsPage> =
    when (subActionType) {
        ProfileSubActionType.Actions -> getProfileActions(username = username, page = page)
            .mapCatching { it.toProfileListItemsPage() }

        ProfileSubActionType.EntriesAdded -> getProfileEntriesAdded(username = username, page = page)
            .mapCatching { it.toProfileListItemsPage() }

        ProfileSubActionType.EntriesVoted -> getProfileEntriesVoted(username = username, page = page)
            .mapCatching { it.toProfileListItemsPage() }

        ProfileSubActionType.EntriesCommented -> getProfileEntriesCommented(username = username, page = page)
            .mapCatching { it.toProfileListItemsPage() }

        ProfileSubActionType.LinksAdded -> getProfileLinksAdded(username = username, page = page)
            .mapCatching { it.toProfileListItemsPage() }

        ProfileSubActionType.LinksPublished -> getProfileLinksPublished(username = username, page = page)
            .mapCatching { it.toProfileListItemsPage() }

        ProfileSubActionType.LinksUp -> getProfileLinksUp(username = username, page = page)
            .mapCatching { it.toProfileListItemsPage() }

        ProfileSubActionType.LinksDown -> getProfileLinksDown(username = username, page = page)
            .mapCatching { it.toProfileListItemsPage() }

        ProfileSubActionType.LinksCommented -> getProfileLinksCommented(username = username, page = page)
            .mapCatching { it.toProfileListItemsPage() }

        ProfileSubActionType.LinksRelated -> getProfileLinksRelated(username = username, page = page)
            .mapCatching { it.toProfileListItemsPage() }

        ProfileSubActionType.Followers -> getProfileObservedUsersFollowers(username = username, page = page)
            .mapCatching { it.toProfileListItemsPage() }

        ProfileSubActionType.FollowingTags -> getProfileObservedTags(username = username, page = page)
            .mapCatching { it.toProfileListItemsPage() }

        ProfileSubActionType.FollowingUsers -> getProfileObservedUsersFollowing(username = username, page = page)
            .mapCatching { it.toProfileListItemsPage() }
    }

internal fun Resources.toProfileListItemsPage(): ProfileListItemsPage = ProfileListItemsPage(
    data = data.map { resource -> ProfileListItem.Resource(resource) },
    pagination = pagination,
)

internal fun ObservedUsers.toProfileListItemsPage(): ProfileListItemsPage = ProfileListItemsPage(
    data = data.map { user -> ProfileListItem.ObservedUserItem(user) },
    pagination = pagination,
)

internal fun ObservedTags.toProfileListItemsPage(): ProfileListItemsPage = ProfileListItemsPage(
    data = data.map { tag -> ProfileListItem.ObservedTagItem(tag) },
    pagination = pagination,
)

internal fun ObservedUser.toItemState(): UserItemState = UserItemState(
    username = username,
    avatarUrl = avatar,
    genderIndicatorType = gender.toGenderIndicatorType(),
    nameColorType = color.toNameColorType(),
    online = online,
    company = company,
    verified = verified,
    status = status,
)

internal fun ObservedTag.toItemState(): ProfileObservedTagItemState = ProfileObservedTagItemState(
    name = name,
    pinned = pinned,
)

internal fun ImmutableList<ProfileSummaryItem>.updateFollowersCount(delta: Int): ImmutableList<ProfileSummaryItem> =
    map { item ->
        if (item is ProfileSummaryItem.Followers) {
            ProfileSummaryItem.Followers((item.value + delta).coerceAtLeast(0))
        } else {
            item
        }
    }.toImmutableList()

internal fun List<ProfileListItem>.appendDistinct(incomingItems: List<ProfileListItem>): List<ProfileListItem> {
    val knownIds = mapTo(mutableSetOf()) { it.uniqueKey() }
    val newUniqueItems = incomingItems.filter { item -> knownIds.add(item.uniqueKey()) }
    return this + newUniqueItems
}

internal fun ProfileListItem.uniqueKey(): String =
    when (this) {
        is ProfileListItem.Resource -> "resource_${item.id}"
        is ProfileListItem.ObservedUserItem -> "user_${user.username}"
        is ProfileListItem.ObservedTagItem -> "tag_${tag.name}"
    }
