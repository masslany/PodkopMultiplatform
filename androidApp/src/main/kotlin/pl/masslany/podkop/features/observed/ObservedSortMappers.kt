package pl.masslany.podkop.features.observed

import pl.masslany.podkop.business.observed.domain.models.request.ObservedType
import pl.masslany.podkop.common.models.DropdownMenuItemType

internal fun ObservedType.toDropdownMenuItemType(): DropdownMenuItemType = when (this) {
    ObservedType.All -> DropdownMenuItemType.Everything
    ObservedType.Profiles -> DropdownMenuItemType.Profiles
    ObservedType.Discussions -> DropdownMenuItemType.Discussions
    ObservedType.Tags -> DropdownMenuItemType.Tags
}

internal fun DropdownMenuItemType.toObservedType(): ObservedType = when (this) {
    DropdownMenuItemType.Everything -> ObservedType.All
    DropdownMenuItemType.Profiles -> ObservedType.Profiles
    DropdownMenuItemType.Discussions -> ObservedType.Discussions
    DropdownMenuItemType.Tags -> ObservedType.Tags
    else -> throw IllegalArgumentException("Attempt to convert $this to ObservedType")
}
