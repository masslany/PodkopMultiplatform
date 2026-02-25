package pl.masslany.podkop.features.tag.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.models.DropdownMenuState
import pl.masslany.podkop.features.resources.preview.ResourceItemStateProvider
import pl.masslany.podkop.features.tag.TagScreenState

class TagScreenStateProvider : PreviewParameterProvider<TagScreenState> {
    private val items = ResourceItemStateProvider().values.toList()

    override val values: Sequence<TagScreenState> = sequenceOf(
        TagScreenState.initial.copy(tag = "#ompose"),
        TagScreenState.initial.copy(tag = "compose", isLoading = false, isError = true),
        TagScreenState.initial.copy(
            screenInstanceId = "preview-content",
            tag = "compose",
            bannerUrl = "https://picsum.photos/seed/tag/1200/400",
            isLoading = false,
            isError = false,
            isRefreshing = false,
            isTagContentError = false,
            resources = persistentListOf(items[0], items[1], items[0]),
            sortMenuState = DropdownMenuState(
                items = persistentListOf(
                    DropdownMenuItemType.Active,
                    DropdownMenuItemType.Newest,
                    DropdownMenuItemType.Digged,
                ),
                selected = DropdownMenuItemType.Newest,
                expanded = false,
            ),
            typeMenuState = DropdownMenuState(
                items = persistentListOf(
                    DropdownMenuItemType.Links,
                    DropdownMenuItemType.Entries,
                    DropdownMenuItemType.Everything,
                ),
                selected = DropdownMenuItemType.Everything,
                expanded = false,
            ),
            isPaginating = true,
        ),
    )
}
