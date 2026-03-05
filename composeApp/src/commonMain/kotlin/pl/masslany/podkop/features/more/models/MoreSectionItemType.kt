package pl.masslany.podkop.features.more.models

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_code
import podkop.composeapp.generated.resources.ic_comment
import podkop.composeapp.generated.resources.ic_exclamation
import podkop.composeapp.generated.resources.ic_favorite
import podkop.composeapp.generated.resources.ic_fire
import podkop.composeapp.generated.resources.ic_nav_shovel
import podkop.composeapp.generated.resources.ic_search
import podkop.composeapp.generated.resources.ic_settings
import podkop.composeapp.generated.resources.more_item_about
import podkop.composeapp.generated.resources.more_item_favorites
import podkop.composeapp.generated.resources.more_item_hits
import podkop.composeapp.generated.resources.more_item_messages
import podkop.composeapp.generated.resources.more_item_my_wykop
import podkop.composeapp.generated.resources.more_item_notifications
import podkop.composeapp.generated.resources.more_item_search
import podkop.composeapp.generated.resources.topbar_label_settings

enum class MoreSectionItemType(val titleRes: StringResource, val iconRes: DrawableResource) {
    Notifications(
        titleRes = Res.string.more_item_notifications,
        iconRes = Res.drawable.ic_exclamation,
    ),
    Messages(
        titleRes = Res.string.more_item_messages,
        iconRes = Res.drawable.ic_comment,
    ),
    Favorites(
        titleRes = Res.string.more_item_favorites,
        iconRes = Res.drawable.ic_favorite,
    ),
    Hits(
        titleRes = Res.string.more_item_hits,
        iconRes = Res.drawable.ic_fire,
    ),
    Search(
        titleRes = Res.string.more_item_search,
        iconRes = Res.drawable.ic_search,
    ),
    MyWykop(
        titleRes = Res.string.more_item_my_wykop,
        iconRes = Res.drawable.ic_nav_shovel,
    ),
    Settings(
        titleRes = Res.string.topbar_label_settings,
        iconRes = Res.drawable.ic_settings,
    ),
    About(
        titleRes = Res.string.more_item_about,
        iconRes = Res.drawable.ic_code,
    ),
}
