package pl.masslany.podkop.features.profile.models

sealed class ProfileSummaryItem(open val value: Int, val type: ProfileSummaryType) {
    data class Actions(override val value: Int) :
        ProfileSummaryItem(
            value = value,
            type = ProfileSummaryType.Actions,
        )

    data class Entries(override val value: Int) :
        ProfileSummaryItem(
            value = value,
            type = ProfileSummaryType.Entries,
        )

    data class Links(override val value: Int) :
        ProfileSummaryItem(
            value = value,
            type = ProfileSummaryType.Links,
        )

    data class Followers(override val value: Int) :
        ProfileSummaryItem(
            value = value,
            type = ProfileSummaryType.Followers,
        )

    data class Following(override val value: Int) :
        ProfileSummaryItem(
            value = value,
            type = ProfileSummaryType.Following,
        )
}
