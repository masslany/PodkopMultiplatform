package pl.masslany.podkop.common.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import pl.masslany.podkop.composeapp.R
import pl.masslany.podkop.features.profile.models.MemberSinceState

@Composable
fun MemberSinceState.toMemberSinceLabel(): String? {
    val joined = stringResource(id = R.string.profile_header_joined)
    val ago = stringResource(id = R.string.profile_header_ago)
    return when (this) {
        is MemberSinceState.Days -> {
            "$joined ${
                pluralStringResource(
                    id = R.plurals.profile_header_days_since,
                    count = days,
                    days,
                )
            } $ago"
        }

        is MemberSinceState.Months -> {
            "$joined ${
                pluralStringResource(
                    id = R.plurals.profile_header_months_since,
                    count = months,
                    months,
                )
            } $ago"
        }

        is MemberSinceState.Years -> {
            "$joined ${
                pluralStringResource(
                    id = R.plurals.profile_header_years_since,
                    count = years,
                    years,
                )
            } $ago"
        }

        is MemberSinceState.YearsAndMonths -> {
            "$joined ${
                pluralStringResource(
                    id = R.plurals.profile_header_years_since,
                    count = years,
                    years,
                )
            } ${
                pluralStringResource(
                    id = R.plurals.profile_header_months_since,
                    count = months,
                    months,
                )
            } $ago"
        }

        MemberSinceState.Unknown -> null
    }
}
