package pl.masslany.podkop.common.components.embed.external

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.components.embed.EmbedThumbnailCard
import pl.masslany.podkop.common.components.embed.twitter.FrostedIconBadge
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.embed.EmbedContentType
import pl.masslany.podkop.common.preview.EmbedContentStateProvider
import pl.masslany.podkop.common.preview.PodkopPreview
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_open_in_new
import podkop.composeapp.generated.resources.ic_play_arrow

@Composable
fun ExternalEmbedThumbnail(
    state: EmbedContentState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val iconRes = when (state.type) {
        EmbedContentType.Youtube,
        EmbedContentType.Streamable,
        -> Res.drawable.ic_play_arrow

        else -> Res.drawable.ic_open_in_new
    }

    EmbedThumbnailCard(
        modifier = modifier,
        thumbnailUrl = state.thumbnailUrl,
        sourceLabel = state.sourceLabel,
        onClick = onClick,
        centerOverlay = {
            FrostedIconBadge(iconRes = iconRes)
        },
    )
}

@Preview
@Composable
private fun ExternalEmbedThumbnailPreview(
    @PreviewParameter(EmbedContentStateProvider::class) state: EmbedContentState,
) {
    if (state.type == EmbedContentType.Twitter) return
    PodkopPreview(darkTheme = false) {
        ExternalEmbedThumbnail(
            modifier = Modifier.padding(16.dp),
            state = state,
            onClick = {},
        )
    }
}
