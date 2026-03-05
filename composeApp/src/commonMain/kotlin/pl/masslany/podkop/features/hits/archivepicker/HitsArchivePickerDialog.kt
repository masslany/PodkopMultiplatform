package pl.masslany.podkop.features.hits.archivepicker

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.dialog_button_confirm
import podkop.composeapp.generated.resources.dialog_button_dismiss
import podkop.composeapp.generated.resources.hits_archive_picker_april
import podkop.composeapp.generated.resources.hits_archive_picker_august
import podkop.composeapp.generated.resources.hits_archive_picker_december
import podkop.composeapp.generated.resources.hits_archive_picker_february
import podkop.composeapp.generated.resources.hits_archive_picker_january
import podkop.composeapp.generated.resources.hits_archive_picker_july
import podkop.composeapp.generated.resources.hits_archive_picker_june
import podkop.composeapp.generated.resources.hits_archive_picker_march
import podkop.composeapp.generated.resources.hits_archive_picker_may
import podkop.composeapp.generated.resources.hits_archive_picker_next_year
import podkop.composeapp.generated.resources.hits_archive_picker_november
import podkop.composeapp.generated.resources.hits_archive_picker_october
import podkop.composeapp.generated.resources.hits_archive_picker_previous_year
import podkop.composeapp.generated.resources.hits_archive_picker_september
import podkop.composeapp.generated.resources.hits_archive_picker_title
import podkop.composeapp.generated.resources.ic_arrow_back

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
            Text(text = stringResource(resource = Res.string.hits_archive_picker_title))
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
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = vectorResource(resource = Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(
                                resource = Res.string.hits_archive_picker_previous_year,
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
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(180f),
                            imageVector = vectorResource(resource = Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(
                                resource = Res.string.hits_archive_picker_next_year,
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
                                        text = stringResource(resource = monthState.month.toMonthLabel()),
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
                Text(text = stringResource(resource = Res.string.dialog_button_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(resource = Res.string.dialog_button_dismiss))
            }
        },
    )
}

@Composable
private fun Int.toMonthLabel(): StringResource =
    when (this) {
        1 -> Res.string.hits_archive_picker_january
        2 -> Res.string.hits_archive_picker_february
        3 -> Res.string.hits_archive_picker_march
        4 -> Res.string.hits_archive_picker_april
        5 -> Res.string.hits_archive_picker_may
        6 -> Res.string.hits_archive_picker_june
        7 -> Res.string.hits_archive_picker_july
        8 -> Res.string.hits_archive_picker_august
        9 -> Res.string.hits_archive_picker_september
        10 -> Res.string.hits_archive_picker_october
        11 -> Res.string.hits_archive_picker_november
        12 -> Res.string.hits_archive_picker_december
        else -> throw IllegalArgumentException("Unknown month $this")
    }
