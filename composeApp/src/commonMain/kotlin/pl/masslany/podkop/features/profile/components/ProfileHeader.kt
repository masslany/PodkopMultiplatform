package pl.masslany.podkop.features.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest.Builder
import coil3.request.crossfade
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.components.GenderIndicator
import pl.masslany.podkop.common.components.toComposeColor
import pl.masslany.podkop.features.profile.MemberSinceState
import pl.masslany.podkop.features.profile.ProfileHeaderState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_profile
import podkop.composeapp.generated.resources.profile_header_ago
import podkop.composeapp.generated.resources.profile_header_days_since
import podkop.composeapp.generated.resources.profile_header_joined
import podkop.composeapp.generated.resources.profile_header_months_since
import podkop.composeapp.generated.resources.profile_header_years_since

@Composable
fun ProfileHeader(
    state: ProfileHeaderState,
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
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 120.dp,
                    ),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    if (state.avatarUrl.isNotBlank()) {
                        AsyncImage(
                            modifier = Modifier
                                .size(100.dp)
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
                                .size(100.dp)
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

                    Spacer(modifier = Modifier.size(4.dp))
                    GenderIndicator(
                        type = state.genderIndicatorType,
                        modifier = Modifier
                            .width(100.dp)
                            .height(4.dp),
                    )
                }

                Spacer(modifier = Modifier.size(16.dp))

                Column {
                    Text(
                        text = state.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = state.nameColorType.toComposeColor(),
                    )
                    memberSinceLabel(memberSinceState = state.memberSinceState)?.let {
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun memberSinceLabel(memberSinceState: MemberSinceState): String? {
    val joined = stringResource(resource = Res.string.profile_header_joined)
    val ago = stringResource(resource = Res.string.profile_header_ago)
    return when (memberSinceState) {
        is MemberSinceState.Days -> {
            "$joined ${
                pluralStringResource(
                    resource = Res.plurals.profile_header_days_since,
                    quantity = memberSinceState.days,
                    memberSinceState.days,
                )
            } $ago"
        }

        is MemberSinceState.Months -> {
            "$joined ${
                pluralStringResource(
                    resource = Res.plurals.profile_header_months_since,
                    quantity = memberSinceState.months,
                    memberSinceState.months,
                )
            } $ago"
        }

        is MemberSinceState.Years -> {
            "$joined ${
                pluralStringResource(
                    resource = Res.plurals.profile_header_years_since,
                    quantity = memberSinceState.years,
                    memberSinceState.years,
                )
            } $ago"
        }

        is MemberSinceState.YearsAndMonths -> {
            "$joined ${
                pluralStringResource(
                    resource = Res.plurals.profile_header_years_since,
                    quantity = memberSinceState.years,
                    memberSinceState.years,
                )
            } ${
                pluralStringResource(
                    resource = Res.plurals.profile_header_months_since,
                    quantity = memberSinceState.months,
                    memberSinceState.months,
                )
            } $ago"
        }

        MemberSinceState.Unknown -> null
    }
}
