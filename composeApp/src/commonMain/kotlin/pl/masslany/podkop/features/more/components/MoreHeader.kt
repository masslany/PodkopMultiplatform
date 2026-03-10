package pl.masslany.podkop.features.more.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.features.more.MoreScreenState
import pl.masslany.podkop.features.profile.components.ProfileHeader
import pl.masslany.podkop.features.profile.models.MemberSinceState
import pl.masslany.podkop.features.profile.models.ProfileHeaderState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_profile
import podkop.composeapp.generated.resources.more_profile_preview_unavailable
import podkop.composeapp.generated.resources.profile_log_in_button

@Composable
fun MoreHeader(
    state: MoreScreenState,
    onProfileClicked: () -> Unit,
    onLoginClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoggedIn) {
        val fallbackHeaderState = ProfileHeaderState(
            username = stringResource(resource = Res.string.more_profile_preview_unavailable),
            avatarUrl = "",
            rankPosition = null,
            backgroundUrl = "",
            genderIndicatorType = GenderIndicatorType.Unspecified,
            nameColorType = NameColorType.Orange,
            memberSinceState = MemberSinceState.Unknown,
            isLoggedIn = true,
            isOwnProfile = true,
            isObserved = false,
            isBlacklisted = false,
            canManageObservation = false,
            canSendPrivateMessage = false,
        )

        Box(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onProfileClicked),
        ) {
            ProfileHeader(
                state = state.profileHeader ?: fallbackHeaderState,
                isDetailsExpanded = false,
                isDetailsToggleVisible = false,
                onDetailsToggleClicked = {},
            )
        }
    } else {
        MoreLoggedOutHeader(
            modifier = modifier,
            onLoginClicked = onLoginClicked,
        )
    }
}

@Composable
private fun MoreLoggedOutHeader(
    onLoginClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 12.dp,
                ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 10.dp,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)),
                ) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(resource = Res.drawable.ic_profile),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onLoginClicked,
                ) {
                    Text(
                        text = stringResource(resource = Res.string.profile_log_in_button),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MoreHeaderLoggedOutPreview() {
    PodkopPreview(darkTheme = false) {
        MoreHeader(
            state = MoreScreenState(
                isLoading = false,
                isLoggedIn = false,
                profileHeader = null,
                sections = persistentListOf(),
            ),
            onProfileClicked = {},
            onLoginClicked = {},
        )
    }
}

@Preview
@Composable
private fun MoreHeaderLoggedInPreview() {
    PodkopPreview(darkTheme = false) {
        MoreHeader(
            state = MoreScreenState(
                isLoading = false,
                isLoggedIn = true,
                profileHeader = ProfileHeaderState(
                    username = "patryk",
                    avatarUrl = "",
                    rankPosition = 176,
                    backgroundUrl = "",
                    genderIndicatorType = GenderIndicatorType.Unspecified,
                    nameColorType = NameColorType.Orange,
                    memberSinceState = MemberSinceState.Years(years = 2),
                    isLoggedIn = true,
                    isOwnProfile = true,
                    isObserved = false,
                    isBlacklisted = false,
                    canManageObservation = false,
                    canSendPrivateMessage = false,
                ),
                sections = persistentListOf(),
            ),
            onProfileClicked = {},
            onLoginClicked = {},
        )
    }
}
