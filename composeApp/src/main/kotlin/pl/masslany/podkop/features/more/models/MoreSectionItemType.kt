package pl.masslany.podkop.features.more.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import pl.masslany.podkop.composeapp.R

enum class MoreSectionItemType(@StringRes val titleRes: Int, @DrawableRes val iconRes: Int) {
    Notifications(
        titleRes = R.string.more_item_notifications,
        iconRes = R.drawable.ic_notifications,
    ),
    Messages(
        titleRes = R.string.more_item_messages,
        iconRes = R.drawable.ic_comment,
    ),
    Favorites(
        titleRes = R.string.more_item_favorites,
        iconRes = R.drawable.ic_favorite,
    ),
    Hits(
        titleRes = R.string.more_item_hits,
        iconRes = R.drawable.ic_fire,
    ),
    Search(
        titleRes = R.string.more_item_search,
        iconRes = R.drawable.ic_search,
    ),
    Rank(
        titleRes = R.string.more_item_rank,
        iconRes = R.drawable.ic_leaderboard,
    ),
    MyWykop(
        titleRes = R.string.more_item_my_wykop,
        iconRes = R.drawable.ic_observed,
    ),
    Settings(
        titleRes = R.string.topbar_label_settings,
        iconRes = R.drawable.ic_settings,
    ),
    About(
        titleRes = R.string.more_item_about,
        iconRes = R.drawable.ic_code,
    ),
}
