package pl.masslany.podkop.common.components.embed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.common.components.embed.external.ExternalEmbedThumbnail
import pl.masslany.podkop.common.components.embed.twitter.TwitterEmbedContent
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.embed.EmbedContentType

val MediumMinWidth = 600.dp

@Composable
fun EmbedContent(
    state: EmbedContentState,
    modifier: Modifier = Modifier,
    onPreviewClick: () -> Unit,
    onFetchedContentClick: () -> Unit,
) {
    when (state.type) {
        EmbedContentType.Twitter -> TwitterEmbedContent(
            modifier = modifier,
            state = state,
            onPreviewClick = onPreviewClick,
            onFetchedContentClick = onFetchedContentClick,
        )

        EmbedContentType.Youtube,
        EmbedContentType.Streamable,
        EmbedContentType.Other,
        -> ExternalEmbedThumbnail(
            modifier = modifier,
            state = state,
            onClick = onPreviewClick,
        )
    }
}
