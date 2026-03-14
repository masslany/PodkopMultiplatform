package pl.masslany.podkop.features.resourceactions.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import pl.masslany.podkop.common.preview.PreviewFixtures
import pl.masslany.podkop.features.resourceactions.ResourceActionsBottomSheetState
import pl.masslany.podkop.features.resourceactions.ResourceActionsParams
import pl.masslany.podkop.features.resourceactions.ResourceActionsType
import pl.masslany.podkop.features.resourceactions.buildState

class ResourceActionsBottomSheetStateProvider : PreviewParameterProvider<ResourceActionsBottomSheetState> {
    override val values: Sequence<ResourceActionsBottomSheetState> = sequenceOf(
        buildState(
            params = ResourceActionsParams(
                resourceType = ResourceActionsType.Entry,
                rootId = 77,
                screenshotDraftId = "draft-entry",
                canDelete = true,
                canEdit = true,
                content = PreviewFixtures.LONG_BODY,
                copyContent = PreviewFixtures.LONG_BODY,
                photoKey = "entry-photo",
                photoUrl = "https://picsum.photos/seed/entry-preview/800/600",
            ),
        ),
        buildState(
            params = ResourceActionsParams(
                resourceType = ResourceActionsType.LinkComment,
                rootId = 42,
                rootSlug = "compose-preview",
                parentId = 100,
                childId = 101,
                screenshotDraftId = "draft-link-comment",
                canEdit = true,
                content = "The comment is editable and shareable as a screenshot.",
                copyContent = "The comment is editable and shareable as a screenshot.",
            ),
        ),
        buildState(
            params = ResourceActionsParams(
                resourceType = ResourceActionsType.Link,
                rootId = 42,
                rootSlug = "compose-preview",
            ),
        ),
    )
}
