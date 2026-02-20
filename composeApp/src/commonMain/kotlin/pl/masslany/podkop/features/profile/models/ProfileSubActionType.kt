package pl.masslany.podkop.features.profile.models

sealed interface ProfileSubActionType {
    val summaryType: ProfileSummaryType

    data object Actions : ProfileSubActionType {
        override val summaryType = ProfileSummaryType.Actions
    }

    data object EntriesAdded : ProfileSubActionType {
        override val summaryType = ProfileSummaryType.Entries
    }

    data object EntriesVoted : ProfileSubActionType {
        override val summaryType = ProfileSummaryType.Entries
    }

    data object EntriesCommented : ProfileSubActionType {
        override val summaryType = ProfileSummaryType.Entries
    }

    data object LinksAdded : ProfileSubActionType {
        override val summaryType = ProfileSummaryType.Links
    }

    data object LinksPublished : ProfileSubActionType {
        override val summaryType = ProfileSummaryType.Links
    }

    data object LinksUp : ProfileSubActionType {
        override val summaryType = ProfileSummaryType.Links
    }

    data object LinksDown : ProfileSubActionType {
        override val summaryType = ProfileSummaryType.Links
    }

    data object LinksCommented : ProfileSubActionType {
        override val summaryType = ProfileSummaryType.Links
    }

    data object LinksRelated : ProfileSubActionType {
        override val summaryType = ProfileSummaryType.Links
    }

    data object Followers : ProfileSubActionType {
        override val summaryType = ProfileSummaryType.Followers
    }

    data object FollowingTags : ProfileSubActionType {
        override val summaryType = ProfileSummaryType.Following
    }

    data object FollowingUsers : ProfileSubActionType {
        override val summaryType = ProfileSummaryType.Following
    }
}

fun ProfileSubActionType.isResourceBacked(): Boolean {
    return when (this) {
        ProfileSubActionType.Followers,
        ProfileSubActionType.FollowingTags,
        ProfileSubActionType.FollowingUsers,
            -> false

        else -> true
    }
}

fun ProfileSubActionType.isObservedUsers(): Boolean {
    return this == ProfileSubActionType.Followers || this == ProfileSubActionType.FollowingUsers
}

fun ProfileSubActionType.isObservedTags(): Boolean {
    return this == ProfileSubActionType.FollowingTags
}
