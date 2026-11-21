package pl.masslany.podkop.features.links.hits.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import pl.masslany.podkop.common.preview.PreviewFixtures
import pl.masslany.podkop.features.links.hits.models.HitItemState
import pl.masslany.podkop.features.resources.models.ResourceType

class HitItemStateProvider : PreviewParameterProvider<HitItemState> {
    override val values: Sequence<HitItemState> = sequenceOf(
        HitItemState(
            id = 1,
            contentType = ResourceType.HitItem,
            titleState = PreviewFixtures.titleState(title = "Hit without image", maxLines = 2),
            countState = PreviewFixtures.countState(count = "2048", isHot = true),
            imageUrl = "",
            isAdult = false,
        ),
        HitItemState(
            id = 2,
            contentType = ResourceType.HitItem,
            titleState = PreviewFixtures.titleState(title = "Hit with image", maxLines = 2),
            countState = PreviewFixtures.countState(count = "512", isHot = false),
            imageUrl = "https://picsum.photos/seed/hit/480/320",
            isAdult = false,
        ),
        HitItemState(
            id = 3,
            contentType = ResourceType.HitItem,
            titleState = PreviewFixtures.titleState(
                title = "Adult hit",
                maxLines = 2,
                isAdult = true,
                displayAdultBadge = true,
            ),
            countState = PreviewFixtures.countState(count = "18", isHot = false, isVoted = true),
            imageUrl = "",
            isAdult = true,
        ),
    )
}
