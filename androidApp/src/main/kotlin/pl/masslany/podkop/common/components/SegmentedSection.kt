package pl.masslany.podkop.common.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A titled group of Material 3 Expressive segmented rows. Give each row
 * `ListItemDefaults.segmentedShapes(index, count)` so the group reads as one rounded block.
 */
@Composable
fun SegmentedSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            content = content,
        )
    }
}

/**
 * The default segment colour matches the screen's surface, so segments use a container colour to
 * stay visible. Checked rows keep it too: their switch already shows the state.
 */
@Composable
fun segmentedRowColors(): ListItemColors {
    val colorScheme = MaterialTheme.colorScheme
    return ListItemDefaults.segmentedColors(
        containerColor = colorScheme.surfaceContainer,
        selectedContainerColor = colorScheme.surfaceContainer,
        selectedContentColor = colorScheme.onSurface,
        selectedSupportingContentColor = colorScheme.onSurfaceVariant,
        selectedLeadingContentColor = colorScheme.onSurfaceVariant,
        selectedTrailingContentColor = colorScheme.onSurfaceVariant,
    )
}

@Composable
fun SectionCardDivider() {
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f),
    )
}
