package pl.masslany.podkop.features.resourceactions.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.features.profile.models.ProfileObservedUserItemState
import pl.masslany.podkop.features.resourceactions.ResourceVotesBottomSheetState
import pl.masslany.podkop.features.resourceactions.ResourceVotesType

data class ResourceVotesBottomSheetPreviewState(
    val state: ResourceVotesBottomSheetState,
    val resourceType: ResourceVotesType,
)

class ResourceVotesBottomSheetStateProvider : PreviewParameterProvider<ResourceVotesBottomSheetPreviewState> {
    private val voters = persistentListOf(
        ProfileObservedUserItemState(
            username = "alice",
            avatarUrl = "https://picsum.photos/seed/alice/96/96",
            genderIndicatorType = GenderIndicatorType.Female,
            nameColorType = NameColorType.Orange,
            online = true,
            company = false,
            verified = true,
            status = "",
        ),
        ProfileObservedUserItemState(
            username = "bob",
            avatarUrl = "https://picsum.photos/seed/bob/96/96",
            genderIndicatorType = GenderIndicatorType.Male,
            nameColorType = NameColorType.Burgundy,
            online = false,
            company = true,
            verified = false,
            status = "",
        ),
    )

    override val values: Sequence<ResourceVotesBottomSheetPreviewState> = sequenceOf(
        ResourceVotesBottomSheetPreviewState(
            state = ResourceVotesBottomSheetState(
                isLoading = true,
            ),
            resourceType = ResourceVotesType.Entry,
        ),
        ResourceVotesBottomSheetPreviewState(
            state = ResourceVotesBottomSheetState(
                isLoading = false,
                isError = true,
            ),
            resourceType = ResourceVotesType.EntryComment,
        ),
        ResourceVotesBottomSheetPreviewState(
            state = ResourceVotesBottomSheetState(
                isLoading = false,
            ),
            resourceType = ResourceVotesType.LinkUp,
        ),
        ResourceVotesBottomSheetPreviewState(
            state = ResourceVotesBottomSheetState(
                isLoading = false,
                isPaginating = true,
                items = voters,
            ),
            resourceType = ResourceVotesType.LinkDown,
        ),
    )
}
