package pl.masslany.podkop.features.more.models

import androidx.annotation.StringRes
import pl.masslany.podkop.R

enum class MoreSectionType(@StringRes val labelRes: Int) {
    Social(labelRes = R.string.more_group_social),
    Content(labelRes = R.string.more_group_content),
    System(labelRes = R.string.more_group_system),
}
