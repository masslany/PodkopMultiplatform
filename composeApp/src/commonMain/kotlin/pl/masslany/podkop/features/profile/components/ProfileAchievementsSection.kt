package pl.masslany.podkop.features.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.features.profile.models.ProfileAchievementItemState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.profile_achievements_empty
import podkop.composeapp.generated.resources.profile_achievements_error
import podkop.composeapp.generated.resources.profile_achievements_title
import podkop.composeapp.generated.resources.profile_badge_achieved_at

@Composable
internal fun ProfileAchievementsSection(
    state: pl.masslany.podkop.features.profile.models.ProfileAchievementsSectionState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(resource = Res.string.profile_achievements_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }

                state.isError -> {
                    Text(
                        text = stringResource(resource = Res.string.profile_achievements_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                state.items.isEmpty() -> {
                    Text(
                        text = stringResource(resource = Res.string.profile_achievements_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.items.forEach { item ->
                            ProfileAchievementBadge(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileAchievementBadge(
    item: ProfileAchievementItemState,
) {
    var expanded by rememberSaveable(item.slug) { mutableStateOf(false) }
    val accentColor = item.toAccentColor(isDarkTheme = isSystemInDarkTheme())
    val backgroundColor = accentColor // .copy(alpha = 0.12f)

    Box {
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .clickable { expanded = true }
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (item.iconUrl.isNotBlank()) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = item.iconUrl,
                    contentDescription = item.label,
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text(
                    text = item.label,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleSmall,
                )
                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (item.achievedAt.isNotBlank()) {
                    Text(
                        text = stringResource(resource = Res.string.profile_badge_achieved_at, item.achievedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAchievementItemState.toAccentColor(isDarkTheme: Boolean): Color {
    val raw = when {
        isDarkTheme && colorHexDark.isNotBlank() -> colorHexDark
        colorHex.isNotBlank() -> colorHex
        else -> colorHexDark
    }
    val hex = raw.removePrefix("#")
    val parsed = hex.toLongOrNull(radix = 16) ?: return MaterialTheme.colorScheme.onSurfaceVariant
    return Color(0xFF000000 or parsed)
}
