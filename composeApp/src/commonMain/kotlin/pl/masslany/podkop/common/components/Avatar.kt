package pl.masslany.podkop.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.jetbrains.compose.resources.painterResource
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_profile

@Composable
fun Avatar(
    state: AvatarState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
    ) {
        AvatarImageTypeRouter(avatarType = state.type)
        Spacer(Modifier.size(4.dp))
        GenderIndicator(
            type = state.genderIndicatorType,
            modifier = Modifier
                .width(36.dp)
                .height(2.dp)
        )

    }
}

@Composable
fun AvatarImageTypeRouter(avatarType: AvatarType) {
    when (avatarType) {
        is AvatarType.NetworkImage -> {
            SubcomposeAsyncImage(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.small),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(avatarType.url)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                    )
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                    ) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(resource = Res.drawable.ic_profile),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    }
                },
            )
        }
        AvatarType.NoAvatar -> {
            Image(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.small),
                painter = painterResource(resource = Res.drawable.ic_profile),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
