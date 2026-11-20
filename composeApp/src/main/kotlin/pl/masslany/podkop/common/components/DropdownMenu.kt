package pl.masslany.podkop.common.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import pl.masslany.podkop.common.models.DropdownMenuItemType
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.composeapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenu(
    items: ImmutableList<DropdownMenuItemType>,
    selected: DropdownMenuItemType,
    expanded: Boolean,
    onSelected: (DropdownMenuItemType) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {
        FilterChip(
            selected = true,
            onClick = {
                if (expanded) {
                    onDismissRequest()
                }
            },
            label = {
                Text(
                    text = selected.toText(),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            },
            trailingIcon = {
                Icon(
                    modifier = Modifier.rotate(if (expanded) EXPANDED_ROTATION else DEFAULT_ROTATION),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_dropdown),
                    contentDescription = null,
                )
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
        ) {
            items.forEach { dropdownMenuItem ->
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelected(dropdownMenuItem) },
                ) {
                    Text(
                        text = dropdownMenuItem.toText(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun DropdownMenuItemType.toText(): String = when (this) {
    DropdownMenuItemType.Active ->
        stringResource(id = R.string.dropdown_menu_label_active)

    DropdownMenuItemType.Newest ->
        stringResource(id = R.string.dropdown_menu_label_newest)

    DropdownMenuItemType.Commented ->
        stringResource(id = R.string.dropdown_menu_label_commented)

    DropdownMenuItemType.Digged ->
        stringResource(id = R.string.dropdown_menu_label_digged)

    DropdownMenuItemType.Best ->
        stringResource(id = R.string.dropdown_menu_label_best)

    DropdownMenuItemType.Oldest ->
        stringResource(id = R.string.dropdown_menu_label_oldest)

    DropdownMenuItemType.Hot ->
        stringResource(id = R.string.dropdown_menu_label_hot)

    DropdownMenuItemType.TwoHours ->
        stringResource(id = R.string.dropdown_menu_label_hot_2h)

    DropdownMenuItemType.SixHours ->
        stringResource(id = R.string.dropdown_menu_label_hot_6h)

    DropdownMenuItemType.TwelveHours ->
        stringResource(id = R.string.dropdown_menu_label_hot_12h)

    DropdownMenuItemType.TwentyFourHours ->
        stringResource(id = R.string.dropdown_menu_label_hot_24h)

    DropdownMenuItemType.All ->
        stringResource(id = R.string.dropdown_menu_label_all)

    DropdownMenuItemType.Day ->
        stringResource(id = R.string.dropdown_menu_label_day)

    DropdownMenuItemType.Week ->
        stringResource(id = R.string.dropdown_menu_label_week)

    DropdownMenuItemType.Month ->
        stringResource(id = R.string.dropdown_menu_label_month)

    DropdownMenuItemType.Year ->
        stringResource(id = R.string.dropdown_menu_label_year)

    DropdownMenuItemType.Entries ->
        stringResource(id = R.string.dropdown_menu_label_entries)

    DropdownMenuItemType.Links ->
        stringResource(id = R.string.dropdown_menu_label_links)

    DropdownMenuItemType.Everything ->
        stringResource(id = R.string.dropdown_menu_label_everything)

    DropdownMenuItemType.Profiles ->
        stringResource(id = R.string.dropdown_menu_label_profiles)

    DropdownMenuItemType.Discussions ->
        stringResource(id = R.string.dropdown_menu_label_discussions)

    DropdownMenuItemType.Tags ->
        stringResource(id = R.string.dropdown_menu_label_tags)

    DropdownMenuItemType.LinkComments ->
        stringResource(id = R.string.dropdown_menu_label_link_comments)

    DropdownMenuItemType.EntryComments ->
        stringResource(id = R.string.dropdown_menu_label_entry_comments)
}

private const val DEFAULT_ROTATION = 0f
private const val EXPANDED_ROTATION = 180f

@Preview
@Composable
private fun DropdownMenuPreview() {
    PodkopPreview(darkTheme = false) {
        DropdownMenu(
            modifier = Modifier.padding(16.dp),
            items = persistentListOf(
                DropdownMenuItemType.Active,
                DropdownMenuItemType.Newest,
                DropdownMenuItemType.Digged,
            ),
            selected = DropdownMenuItemType.Active,
            expanded = false,
            onSelected = {},
            onExpandedChange = {},
            onDismissRequest = {},
        )
    }
}
