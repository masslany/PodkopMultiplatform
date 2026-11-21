package pl.masslany.podkop.features.hits.archivepicker

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.masslany.podkop.R

@Composable
fun HitsArchivePickerDialog(
    state: HitsArchivePickerState,
    onDismissRequest: () -> Unit,
    onPreviousYearClick: () -> Unit,
    onNextYearClick: () -> Unit,
    onMonthClick: (Int) -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(id = R.string.hits_archive_picker_title))
        },
        text = {
            Column(
                modifier = Modifier.widthIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        enabled = state.isPreviousYearEnabled,
                        onClick = onPreviousYearClick,
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(
                                id = R.string.hits_archive_picker_previous_year,
                            ),
                        )
                    }

                    Text(
                        modifier = Modifier.weight(1f),
                        text = state.selectedYear.toString(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                    )

                    IconButton(
                        enabled = state.isNextYearEnabled,
                        onClick = onNextYearClick,
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(180f),
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(
                                id = R.string.hits_archive_picker_next_year,
                            ),
                        )
                    }
                }

                state.months.chunked(4).forEach { monthsRow ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        monthsRow.forEach { monthState ->
                            FilterChip(
                                modifier = Modifier.weight(1f),
                                selected = monthState.isSelected,
                                enabled = monthState.isEnabled,
                                onClick = { onMonthClick(monthState.month) },
                                label = {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = stringResource(id = monthState.month.toMonthLabel()),
                                        textAlign = TextAlign.Center,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(id = R.string.dialog_button_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.dialog_button_dismiss))
            }
        },
    )
}

@Composable
@StringRes
private fun Int.toMonthLabel(): Int =
    when (this) {
        1 -> R.string.hits_archive_picker_january
        2 -> R.string.hits_archive_picker_february
        3 -> R.string.hits_archive_picker_march
        4 -> R.string.hits_archive_picker_april
        5 -> R.string.hits_archive_picker_may
        6 -> R.string.hits_archive_picker_june
        7 -> R.string.hits_archive_picker_july
        8 -> R.string.hits_archive_picker_august
        9 -> R.string.hits_archive_picker_september
        10 -> R.string.hits_archive_picker_october
        11 -> R.string.hits_archive_picker_november
        12 -> R.string.hits_archive_picker_december
        else -> throw IllegalArgumentException("Unknown month $this")
    }
