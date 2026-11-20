package pl.masslany.podkop.common.models

import kotlin.time.Clock
import kotlin.time.Duration.Companion.ZERO
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant

sealed class PublishedTimeType {

    data class Minutes(val minutes: Int) : PublishedTimeType()

    data class HoursMinutes(val hours: Int, val minutes: Int) : PublishedTimeType()

    data class Hours(val hours: Int) : PublishedTimeType()

    data class Days(val days: Int) : PublishedTimeType()

    data class FullDate(val formattedDate: String) : PublishedTimeType()

    data object Now : PublishedTimeType()
}

fun LocalDateTime.toPublishedTimeType(): PublishedTimeType {
    val timeZone = TimeZone.currentSystemDefault()
    val now = Clock.System.now()
    var age = now - this.toInstant(timeZone)
    if (age < ZERO) age = ZERO // future dates → treat as "now"

    // "yyyy.MM.dd HH:mm"
    val formattedFullDate = buildString {
        append(this@toPublishedTimeType.year)
        append('.')
        append(this@toPublishedTimeType.month.number.toString().padStart(2, '0'))
        append('.')
        append(this@toPublishedTimeType.day.toString().padStart(2, '0'))
        append(" ")
        append(this@toPublishedTimeType.hour.toString().padStart(2, '0'))
        append(':')
        append(this@toPublishedTimeType.minute.toString().padStart(2, '0'))
    }

    return when {
        age.inWholeDays > DAYS_IN_WEEK -> PublishedTimeType.FullDate(formattedFullDate)

        age.inWholeHours >= HOURS_IN_DAY -> PublishedTimeType.Days(age.inWholeDays.toInt())

        age.inWholeHours >= 1 -> {
            val hours = age.inWholeHours.toInt()
            val minutes = (age.inWholeMinutes % MINUTES_IN_HOUR).toInt()
            if (minutes > 0) {
                PublishedTimeType.HoursMinutes(hours, minutes)
            } else {
                PublishedTimeType.Hours(hours)
            }
        }

        age.inWholeMinutes > 0 -> PublishedTimeType.Minutes(age.inWholeMinutes.toInt())

        else -> PublishedTimeType.Now
    }
}

private const val DAYS_IN_WEEK = 7
private const val HOURS_IN_DAY = 24
private const val MINUTES_IN_HOUR = 60
