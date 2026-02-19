package pl.masslany.podkop.features.resources.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.components.Author
import pl.masslany.podkop.common.components.CommentCount
import pl.masslany.podkop.common.components.Count
import pl.masslany.podkop.common.components.Description
import pl.masslany.podkop.common.components.Dot
import pl.masslany.podkop.common.components.Published
import pl.masslany.podkop.common.components.Source
import pl.masslany.podkop.common.components.Tag
import pl.masslany.podkop.common.components.Title
import pl.masslany.podkop.common.models.AuthorState
import pl.masslany.podkop.common.models.CountState
import pl.masslany.podkop.common.models.DescriptionState
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.TagItem
import pl.masslany.podkop.common.models.TitleState
import pl.masslany.podkop.common.theme.PodkopTheme
import pl.masslany.podkop.features.resources.models.ResourceType
import pl.masslany.podkop.features.resources.models.link.LinkItemState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LinkItem(
    modifier: Modifier = Modifier,
    state: LinkItemState,
    onLinkClick: () -> Unit,
    onVoteClick: () -> Unit,
    onAuthorClick: (String) -> Unit,
    onTagClick: (String) -> Unit,
    onSourceClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .clip(CardDefaults.shape)
            .clickable { onLinkClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        ),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                    ),
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                ) {
                    Count(
                        state = state.countState,
                        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        onClick = onVoteClick,
                    )
                }
                state.titleState?.let {
                    Spacer(modifier = Modifier.size(12.dp))
                    Title(state = state.titleState)
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    state.descriptionState?.let {
                        Description(
                            modifier = Modifier.weight(1f),
                            state = state.descriptionState,
                        )
                    }
                    if (state.imageUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.size(8.dp))
                        AsyncImage(
                            modifier = Modifier
                                .height(80.dp)
                                .width(80.dp)
                                .clip(MaterialTheme.shapes.small),
                            model = ImageRequest.Builder(LocalPlatformContext.current)
                                .data(state.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                FlowRow {
                    state.authorState?.let {
                        Author(
                            state = state.authorState,
                            onClick = onAuthorClick,
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Dot()
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    state.source?.let {
                        Source(
                            source = state.source,
                            onSourceClick = onSourceClick,
                        )
                    }
                    state.publishedTimeType?.let {
                        Spacer(modifier = Modifier.size(4.dp))
                        Dot()
                        Spacer(modifier = Modifier.size(4.dp))
                        Published(type = state.publishedTimeType)
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                ) {
                    FlowRow(
                        modifier = Modifier
                            .weight(1f),
                    ) {
                        state.tags.forEach { tag ->
                            Tag(
                                state = tag,
                                onTagClick = onTagClick,
                            )
                            if (tag.needsSpacer) {
                                Spacer(modifier = Modifier.size(4.dp))
                                Dot()
                                Spacer(modifier = Modifier.size(4.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    CommentCount(count = state.commentCount)
                }
                Spacer(modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Preview
@Composable
private fun LinkItemPreview() {
    PodkopTheme {
        LinkItem(
            state = LinkItemState(
                id = 0,
                titleState = TitleState(
                    title = "Title",
                    maxLines = Int.MAX_VALUE,
                    isAdult = false,
                    displayAdultBadge = true,
                ),
                descriptionState = DescriptionState(
                    "Description desc desc desc. ".repeat(5),
                    3,
                ),
                countState = CountState(
                    count = "300",
                    isHot = false,
                    isVoted = false,
                    canVote = false,
                ),
                authorState = AuthorState("Username", NameColorType.Orange),
                source = "test.website",
                sourceUrl = "https://test.website",
                imageUrl = "",
                tags = persistentListOf(
                    TagItem("#test", true),
                    TagItem("#test2", true),
                    TagItem("#test3", false),
                ),
                comments = persistentListOf(),
                commentCount = 10,
                publishedTimeType = PublishedTimeType.Now,
                contentType = ResourceType.LinkItem,
            ),
            onTagClick = {},
            onVoteClick = {},
            onLinkClick = {},
            onAuthorClick = {},
            onSourceClick = {},
        )
    }
}
