package pl.masslany.podkop.common.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.common.models.PublishedTimeType
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.published_at_days
import podkop.composeapp.generated.resources.published_at_hours
import podkop.composeapp.generated.resources.published_at_hours_minutes
import podkop.composeapp.generated.resources.published_at_minutes
import podkop.composeapp.generated.resources.published_at_now

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
        pluralStringResource(resource = Res.plurals.published_at_days, this.days, this.days)

    is PublishedTimeType.HoursMinutes ->
        stringResource(
            resource = Res.string.published_at_hours_minutes,
            this.hours,
            this.minutes,
        )

    is PublishedTimeType.Hours ->
        stringResource(resource = Res.string.published_at_hours, this.hours)

    is PublishedTimeType.Minutes ->
        stringResource(resource = Res.string.published_at_minutes, this.minutes)

    is PublishedTimeType.Now ->
        stringResource(resource = Res.string.published_at_now)
}
