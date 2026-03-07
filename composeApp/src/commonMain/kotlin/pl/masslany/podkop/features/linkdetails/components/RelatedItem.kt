package pl.masslany.podkop.features.linkdetails.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import pl.masslany.podkop.common.components.Author
import pl.masslany.podkop.common.components.Source
import pl.masslany.podkop.common.components.Title
import pl.masslany.podkop.common.components.vote.Vote
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.preview.PreviewFixtures
import pl.masslany.podkop.features.resources.models.ResourceType
import pl.masslany.podkop.features.resources.models.related.RelatedItemState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.no_image

@Composable
fun RelatedItem(
    state: RelatedItemState,
    modifier: Modifier,
    onItemClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onSourceClick: () -> Unit,
    onVoteUpClick: () -> Unit,
    onVoteDownClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .clip(CardDefaults.shape)
            .clickable { onItemClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight(),
        ) {
            if (state.imageUrl.isEmpty()) {
                Image(
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 8.dp,
                        )
                        .size(48.dp)
                        .aspectRatio(1f)
                        .align(Alignment.CenterVertically),
                    painter = painterResource(resource = Res.drawable.no_image),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                )
            } else {
                AsyncImage(
                    model = state.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f),
                    contentScale = ContentScale.FillHeight,
                )
            }
            Column(
                modifier = Modifier.padding(8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    state.authorState?.let {
                        Author(state = state.authorState, onClick = onAuthorClick)
                    }
                    state.voteState?.let {
                        Vote(
                            state = state.voteState,
                            onVoteUpClick = onVoteUpClick,
                            onVoteDownClick = onVoteDownClick,
                        )
                    }
                }
                state.source?.let {
                    Source(source = state.source, onSourceClick = onSourceClick)
                    Spacer(modifier = Modifier.size(8.dp))
                }
                state.titleState?.let {
                    Title(
                        state = state.titleState,
                        textStyle = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun RelatedItemPreview() {
    val state = RelatedItemState(
        id = 1,
        contentType = ResourceType.RelatedItem,
        imageUrl = "",
        titleState = PreviewFixtures.titleState(maxLines = 2),
        authorState = PreviewFixtures.authorState(),
        source = PreviewFixtures.DOMAIN,
        sourceUrl = PreviewFixtures.URL,
        voteState = PreviewFixtures.voteState(),
    )
    PodkopPreview(darkTheme = false) {
        RelatedItem(
            modifier = Modifier
                .padding(16.dp)
                .height(100.dp),
            state = state,
            onItemClick = {},
            onAuthorClick = {},
            onSourceClick = {},
            onVoteUpClick = {},
            onVoteDownClick = {},
        )
    }
}
