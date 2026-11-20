package pl.masslany.podkop.features.observed.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.observed.ObservedDiscussionBannerState
import pl.masslany.podkop.features.observed.ObservedDiscussionBannerType
import pl.masslany.podkop.features.observed.ObservedListItemState
import pl.masslany.podkop.features.observed.ObservedScreenState
import pl.masslany.podkop.features.observed.toDropdownMenuItemType
import pl.masslany.podkop.features.resources.preview.ResourceItemStateProvider

class ObservedScreenStateProvider : PreviewParameterProvider<ObservedScreenState> {
    private val items = ResourceItemStateProvider().values.toList()

    override val values: Sequence<ObservedScreenState> = sequenceOf(
        ObservedScreenState.initial.copy(
            screenInstanceId = "preview-content-all",
            isLoading = false,
            items = persistentListOf(
                ObservedListItemState(
                    key = "entry:1",
                    resource = items[0],
                    discussionBanner = ObservedDiscussionBannerState(
                        newContentCount = 41,
                        type = ObservedDiscussionBannerType.Entry,
                    ),
                ),
                ObservedListItemState(
                    key = "link:2",
                    resource = items[1],
                ),
            ),
            selectedType = ObservedType.All,
            typeMenuState = observedTypeMenu(selected = ObservedType.All.toDropdownMenuItemType()),
        ),
        ObservedScreenState.initial.copy(
            screenInstanceId = "preview-empty-tags",
            isLoading = false,
            items = persistentListOf(),
            selectedType = ObservedType.Tags,
            typeMenuState = observedTypeMenu(selected = ObservedType.Tags.toDropdownMenuItemType()),
        ),
        ObservedScreenState.initial.copy(
            screenInstanceId = "preview-error",
            isLoading = false,
            isError = true,
            items = persistentListOf(),
            selectedType = ObservedType.Profiles,
            typeMenuState = observedTypeMenu(selected = ObservedType.Profiles.toDropdownMenuItemType()),
        ),
    )
}

private fun observedTypeMenu(selected: DropdownMenuItemType) = DropdownMenuState(
    items = persistentListOf(
        DropdownMenuItemType.Everything,
        DropdownMenuItemType.Profiles,
        DropdownMenuItemType.Discussions,
        DropdownMenuItemType.Tags,
    ),
    selected = selected,
    expanded = false,
)
