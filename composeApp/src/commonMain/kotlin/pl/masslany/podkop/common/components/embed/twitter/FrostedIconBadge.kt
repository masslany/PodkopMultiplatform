package pl.masslany.podkop.common.components.embed.twitter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import pl.masslany.podkop.common.preview.PodkopPreview
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.ic_x_logo

private val OverlayBadgeSize = 56.dp
private val OverlayBadgeForegroundSize = 50.dp
private val OverlayIconSize = 24.dp

@Composable
fun FrostedIconBadge(
    iconRes: DrawableResource,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Box(
        modifier = modifier.size(OverlayBadgeSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(OverlayBadgeSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.2f))
                .blur(10.dp),
        )

        Surface(
            modifier = Modifier.size(OverlayBadgeForegroundSize),
            shape = CircleShape,
            color = containerColor,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(resource = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(OverlayIconSize),
                    tint = iconTint,
                )
            }
        }
    }
}

@Composable
fun FrostedLoadingBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(OverlayBadgeSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(OverlayBadgeSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.2f))
                .blur(10.dp),
        )

        Surface(
            modifier = Modifier.size(OverlayBadgeForegroundSize),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            }
        }
    }
}

@Preview
@Composable
private fun FrostedIconBadgePreview() {
    PodkopPreview(darkTheme = false) {
        FrostedIconBadge(iconRes = Res.drawable.ic_x_logo)
    }
}

@Preview
@Composable
private fun FrostedLoadingBadgePreview() {
    PodkopPreview(darkTheme = false) {
        FrostedLoadingBadge()
    }
}
