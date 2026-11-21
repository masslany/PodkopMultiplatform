package pl.masslany.podkop.features.rank.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import pl.masslany.podkop.features.rank.RankScreenState

class RankScreenStateProvider : PreviewParameterProvider<RankScreenState> {
    override val values: Sequence<RankScreenState> = sequenceOf(
        RankScreenState.initial,
        RankPreviewFixtures.errorState(),
        RankPreviewFixtures.contentState(),
        RankPreviewFixtures.contentState(isPaginating = true),
        RankPreviewFixtures.refreshingState(),
    )
}
