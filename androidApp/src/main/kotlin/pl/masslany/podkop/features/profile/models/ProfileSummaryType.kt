package pl.masslany.podkop.features.profile.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

enum class ProfileSummaryType {
    Actions,
    Links,
    Entries,
    Followers,
    Following,
}

fun ProfileSummaryType.toSubActionItems(): ImmutableList<ProfileSubActionType> =
    when (this) {
        ProfileSummaryType.Actions -> persistentListOf(ProfileSubActionType.Actions)

        ProfileSummaryType.Links -> persistentListOf(
            ProfileSubActionType.LinksAdded,
            ProfileSubActionType.LinksPublished,
            ProfileSubActionType.LinksCommented,
            ProfileSubActionType.LinksRelated,
            ProfileSubActionType.LinksUp,
            ProfileSubActionType.LinksDown,
        )

        ProfileSummaryType.Entries -> persistentListOf(
            ProfileSubActionType.EntriesAdded,
            ProfileSubActionType.EntriesCommented,
            ProfileSubActionType.EntriesVoted,
        )

        ProfileSummaryType.Followers -> persistentListOf(ProfileSubActionType.Followers)

        ProfileSummaryType.Following -> persistentListOf(
            ProfileSubActionType.FollowingTags,
            ProfileSubActionType.FollowingUsers,
        )
    }
