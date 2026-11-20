package pl.masslany.podkop.common.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import pl.masslany.podkop.R
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.preview.PodkopPreview
import pl.masslany.podkop.common.preview.PublishedTimeTypeProvider

@Composable
fun Published(
    modifier: Modifier = Modifier,
    type: PublishedTimeType,
) {
    Text(
        modifier = modifier,
        text = type.toText(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
private fun PublishedTimeType.toText(): String = when (this) {
    is PublishedTimeType.FullDate -> this.formattedDate

    is PublishedTimeType.Days ->
        pluralStringResource(id = R.plurals.published_at_days, this.days, this.days)

    is PublishedTimeType.HoursMinutes ->
        stringResource(
            id = R.string.published_at_hours_minutes,
            this.hours,
            this.minutes,
        )

    is PublishedTimeType.Hours ->
        stringResource(id = R.string.published_at_hours, this.hours)

    is PublishedTimeType.Minutes ->
        stringResource(id = R.string.published_at_minutes, this.minutes)

    is PublishedTimeType.Now ->
        stringResource(id = R.string.published_at_now)
}

@Preview
@Composable
private fun PublishedPreview(
    @PreviewParameter(PublishedTimeTypeProvider::class) type: PublishedTimeType,
) {
    PodkopPreview(darkTheme = false) {
        Published(type = type)
    }
}
