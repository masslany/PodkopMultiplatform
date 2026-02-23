package pl.masslany.podkop.common.components.embed.external

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pl.masslany.podkop.common.components.embed.EmbedThumbnailCard
import pl.masslany.podkop.common.components.embed.twitter.FrostedIconBadge
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.embed.EmbedContentType
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
