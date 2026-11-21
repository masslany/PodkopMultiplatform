package pl.masslany.podkop.common.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.R

/** Scroll-to-top FAB that springs in and out with the theme's expressive motion. */
@Composable
fun ScrollToTopFab(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val motionScheme = MaterialTheme.motionScheme
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = scaleIn(motionScheme.fastSpatialSpec()) + fadeIn(motionScheme.fastEffectsSpec()),
        exit = scaleOut(motionScheme.fastSpatialSpec()) + fadeOut(motionScheme.fastEffectsSpec()),
    ) {
        FloatingActionButton(onClick = onClick) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_keyboard_arrow_up),
                contentDescription = stringResource(id = R.string.accessibility_fab_scroll_to_top),
            )
        }
    }
}
