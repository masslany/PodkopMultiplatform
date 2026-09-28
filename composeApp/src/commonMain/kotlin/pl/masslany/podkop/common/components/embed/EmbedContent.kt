package pl.masslany.podkop.common.components.embed

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.masslany.podkop.common.components.embed.external.ExternalEmbedThumbnail
import pl.masslany.podkop.common.components.embed.twitter.TwitterEmbedContent
import pl.masslany.podkop.common.components.embed.video.StreamableEmbedContent
import pl.masslany.podkop.common.models.embed.EmbedContentState
import pl.masslany.podkop.common.models.embed.EmbedContentType
import pl.masslany.podkop.common.preview.EmbedContentStateProvider
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.settings.LocalAppSettings

val MediumMinWidth = 600.dp

@Composable
fun EmbedContent(
    state: EmbedContentState,
    modifier: Modifier = Modifier,
    onPreviewClick: () -> Unit,
    onFetchedContentClick: () -> Unit,
) {
    val playVideosInline by LocalAppSettings.current.playVideosInline
        .collectAsStateWithLifecycle(initialValue = false)

    when (state.type) {
        EmbedContentType.Twitter -> TwitterEmbedContent(
            modifier = modifier,
            state = state,
            onPreviewClick = onPreviewClick,
            onFetchedContentClick = onFetchedContentClick,
        )

        EmbedContentType.Streamable if playVideosInline -> StreamableEmbedContent(
            modifier = modifier,
            state = state,
            onOpenSource = onPreviewClick,
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

@Preview
@Composable
private fun EmbedContentPreview(
    @PreviewParameter(EmbedContentStateProvider::class) state: EmbedContentState,
) {
    PodkopPreview(darkTheme = false) {
        EmbedContent(
            modifier = Modifier.padding(16.dp),
            state = state,
            onPreviewClick = {},
            onFetchedContentClick = {},
        )
    }
}
