package pl.masslany.podkop.features.more.models

import org.jetbrains.compose.resources.StringResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.more_group_content
import podkop.composeapp.generated.resources.more_group_social
import podkop.composeapp.generated.resources.more_group_system

enum class MoreSectionType(val labelRes: StringResource) {
    Social(labelRes = Res.string.more_group_social),
    Content(labelRes = Res.string.more_group_content),
    System(labelRes = Res.string.more_group_system),
}
