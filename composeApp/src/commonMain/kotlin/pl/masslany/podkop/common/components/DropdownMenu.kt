package pl.masslany.podkop.common.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
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
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import pl.masslany.podkop.common.models.DropdownMenuItemType
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.dropdown_menu_label_active
import podkop.composeapp.generated.resources.dropdown_menu_label_all
import podkop.composeapp.generated.resources.dropdown_menu_label_best
import podkop.composeapp.generated.resources.dropdown_menu_label_commented
import podkop.composeapp.generated.resources.dropdown_menu_label_digged
import podkop.composeapp.generated.resources.dropdown_menu_label_entries
import podkop.composeapp.generated.resources.dropdown_menu_label_everything
import podkop.composeapp.generated.resources.dropdown_menu_label_hot
import podkop.composeapp.generated.resources.dropdown_menu_label_hot_12h
import podkop.composeapp.generated.resources.dropdown_menu_label_hot_2h
import podkop.composeapp.generated.resources.dropdown_menu_label_hot_6h
import podkop.composeapp.generated.resources.dropdown_menu_label_links
import podkop.composeapp.generated.resources.dropdown_menu_label_newest
import podkop.composeapp.generated.resources.dropdown_menu_label_oldest
import podkop.composeapp.generated.resources.ic_arrow_dropdown

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
        onExpandedChange = onExpandedChange
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
                    modifier = Modifier.rotate(if (expanded) ExpandedRotation else DefaultRotation),
                    imageVector = vectorResource(resource = Res.drawable.ic_arrow_dropdown),
                    contentDescription = null
                )
            },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
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
private fun DropdownMenuItemType.toText(): String {
    return when (this) {
        DropdownMenuItemType.Active ->
            stringResource(resource = Res.string.dropdown_menu_label_active)

        DropdownMenuItemType.Newest ->
            stringResource(resource = Res.string.dropdown_menu_label_newest)

        DropdownMenuItemType.Commented ->
            stringResource(resource = Res.string.dropdown_menu_label_commented)

        DropdownMenuItemType.Digged ->
            stringResource(resource = Res.string.dropdown_menu_label_digged)

        DropdownMenuItemType.Best ->
            stringResource(resource = Res.string.dropdown_menu_label_best)

        DropdownMenuItemType.Oldest ->
            stringResource(resource = Res.string.dropdown_menu_label_oldest)

        DropdownMenuItemType.Hot ->
            stringResource(resource = Res.string.dropdown_menu_label_hot)

        DropdownMenuItemType.TwoHours ->
            stringResource(resource = Res.string.dropdown_menu_label_hot_2h)

        DropdownMenuItemType.SixHours ->
            stringResource(resource = Res.string.dropdown_menu_label_hot_6h)

        DropdownMenuItemType.TwelveHours ->
            stringResource(resource = Res.string.dropdown_menu_label_hot_12h)

        DropdownMenuItemType.All ->
            stringResource(resource = Res.string.dropdown_menu_label_all)

        DropdownMenuItemType.Entries ->
            stringResource(resource = Res.string.dropdown_menu_label_entries)

        DropdownMenuItemType.Links ->
            stringResource(resource = Res.string.dropdown_menu_label_links)

        DropdownMenuItemType.Everything ->
            stringResource(resource = Res.string.dropdown_menu_label_everything)
    }
}

private const val ExpandedRotation = 180f
private const val DefaultRotation = 0f
