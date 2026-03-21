package pl.masslany.podkop.features.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest.Builder
import coil3.request.crossfade
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.common.components.GenderIndicator
import pl.masslany.podkop.common.components.toComposeColor
import pl.masslany.podkop.common.extensions.toMemberSinceLabel
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.theme.colorsPalette
import pl.masslany.podkop.features.profile.models.MemberSinceState
import pl.masslany.podkop.features.profile.models.ProfileHeaderState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.accessibility_profile_collapse_details
import podkop.composeapp.generated.resources.accessibility_profile_expand_details
import podkop.composeapp.generated.resources.ic_chevron_forward
import podkop.composeapp.generated.resources.ic_profile

@Composable
fun ProfileHeader(
    state: ProfileHeaderState,
    isDetailsExpanded: Boolean,
    isDetailsToggleVisible: Boolean,
    onDetailsToggleClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Box {
            if (state.backgroundUrl.isNotBlank()) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    model = Builder(LocalPlatformContext.current)
                        .data(state.backgroundUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        ),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 120.dp,
                    ),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Box(
                        modifier = Modifier.size(100.dp),
                    ) {
                        if (state.avatarUrl.isNotBlank()) {
                            AsyncImage(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.small),
                                model = Builder(LocalPlatformContext.current)
                                    .data(state.avatarUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                error = painterResource(resource = Res.drawable.ic_profile),
                                placeholder = painterResource(resource = Res.drawable.ic_profile),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
                            ) {
                                Image(
                                    modifier = Modifier.fillMaxSize(),
                                    painter = painterResource(resource = Res.drawable.ic_profile),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                )
                            }
                        }

                        state.rankPosition?.let { rankPosition ->
                            RankBadge(
                                rankPosition = rankPosition,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(4.dp))
                    GenderIndicator(
                        type = state.genderIndicatorType,
                        modifier = Modifier
                            .width(100.dp)
                            .height(4.dp),
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = state.username,
                            style = MaterialTheme.typography.bodyMedium,
                            color = state.nameColorType.toComposeColor(),
                        )
                        state.memberSinceState.toMemberSinceLabel()?.let {
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                    if (isDetailsToggleVisible) {
                        IconButton(onClick = onDetailsToggleClicked) {
                            Icon(
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(if (isDetailsExpanded) 270f else 90f),
                                imageVector = vectorResource(resource = Res.drawable.ic_chevron_forward),
                                contentDescription = stringResource(
                                    resource = if (isDetailsExpanded) {
                                        Res.string.accessibility_profile_collapse_details
                                    } else {
                                        Res.string.accessibility_profile_expand_details
                                    },
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RankBadge(
    rankPosition: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorsPalette.hotOrange)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = "#$rankPosition",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
    }
}

@Preview
@Composable
private fun ProfileHeaderPreview() {
    PodkopPreview(darkTheme = false) {
        ProfileHeader(
            state = ProfileHeaderState(
                username = "patryk",
                avatarUrl = "https://picsum.photos/seed/profile-avatar/160/160",
                rankPosition = 176,
                backgroundUrl = "",
                genderIndicatorType = GenderIndicatorType.Male,
                nameColorType = NameColorType.Orange,
                memberSinceState = MemberSinceState.YearsAndMonths(years = 6, months = 2),
                isLoggedIn = true,
                isOwnProfile = false,
                isObserved = true,
                isBlacklisted = false,
                canManageObservation = true,
                canSendPrivateMessage = true,
            ),
            isDetailsExpanded = false,
            isDetailsToggleVisible = true,
            onDetailsToggleClicked = {},
        )
    }
}
