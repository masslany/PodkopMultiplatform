package pl.masslany.podkop.common.extensions

import kotlin.time.Clock
import kotlin.time.Duration.Companion.ZERO
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import pl.masslany.podkop.features.profile.models.MemberSinceState

fun LocalDateTime?.toMemberSinceState(): MemberSinceState {
    if (this == null) {
        return MemberSinceState.Unknown
    }

    val timeZone = TimeZone.currentSystemDefault()
    var membershipDuration = Clock.System.now() - this.toInstant(timeZone)
    if (membershipDuration < ZERO) {
        membershipDuration = ZERO
    }

    val days = membershipDuration.inWholeDays.toInt()

    return when {
        days >= DAYS_IN_YEAR -> {
            val years = days / DAYS_IN_YEAR
            val months = (days % DAYS_IN_YEAR) / DAYS_IN_MONTH
            if (months == 0) {
                MemberSinceState.Years(years)
            } else {
                MemberSinceState.YearsAndMonths(
                    years = years,
                    months = months,
                )
            }
        }

        days >= DAYS_IN_MONTH -> {
            MemberSinceState.Months(days / DAYS_IN_MONTH)
        }

        else -> {
            MemberSinceState.Days(days)
        }
    }
}

private const val DAYS_IN_MONTH = 30
private const val DAYS_IN_YEAR = 365
