package pl.masslany.podkop.features.linkdetails.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest.Builder
import coil3.request.crossfade
import pl.masslany.podkop.common.components.Author
import pl.masslany.podkop.common.components.Count
import pl.masslany.podkop.common.components.Description
import pl.masslany.podkop.common.components.Dot
import pl.masslany.podkop.common.components.Source
import pl.masslany.podkop.common.components.Tag
import pl.masslany.podkop.common.components.Title
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.resources.models.link.LinkItemState
import pl.masslany.podkop.features.resources.preview.LinkItemStateProvider

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LinkDetailsHeader(
    modifier: Modifier = Modifier,
    state: LinkItemState,
    onLinkClick: () -> Unit,
    onVoteClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onTagClick: (String) -> Unit,

) {
    Column(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                onLinkClick()
            },
        ) {
            if (state.imageUrl.isNotEmpty()) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    model = Builder(LocalPlatformContext.current)
                        .data(state.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Count(
                    state = state.countState,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    onClick = onVoteClick,
                )
                state.titleState?.let {
                    Spacer(modifier = Modifier.size(8.dp))
                    Title(state = state.titleState)
                }
            }
            state.descriptionState?.let {
                Spacer(modifier = Modifier.size(8.dp))
                Description(
                    state = state.descriptionState,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            state.authorState?.let {
                Author(state = state.authorState, onClick = onAuthorClick)
                Spacer(modifier = Modifier.size(4.dp))
                Dot()
            }
            state.source?.let {
                Spacer(modifier = Modifier.size(4.dp))
                Source(source = state.source, onSourceClick = onLinkClick)
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            state.tags.forEach { tag ->
                Tag(state = tag, onTagClick = onTagClick)
                if (tag.needsSpacer) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Dot()
                    Spacer(modifier = Modifier.size(4.dp))
                }
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Preview
@Composable
private fun LinkDetailsHeaderPreview(
    @PreviewParameter(LinkItemStateProvider::class) state: LinkItemState,
) {
    PodkopPreview(darkTheme = false) {
        LinkDetailsHeader(
            modifier = Modifier.padding(bottom = 16.dp),
            state = state,
            onLinkClick = {},
            onVoteClick = {},
            onAuthorClick = {},
            onTagClick = {},
        )
    }
}
