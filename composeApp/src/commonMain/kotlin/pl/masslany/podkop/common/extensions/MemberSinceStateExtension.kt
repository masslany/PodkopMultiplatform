package pl.masslany.podkop.common.extensions

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import pl.masslany.podkop.features.profile.models.MemberSinceState
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.profile_header_ago
import podkop.composeapp.generated.resources.profile_header_days_since
import podkop.composeapp.generated.resources.profile_header_joined
import podkop.composeapp.generated.resources.profile_header_months_since
import podkop.composeapp.generated.resources.profile_header_years_since

@Composable
fun MemberSinceState.toMemberSinceLabel(): String? {
    val joined = stringResource(resource = Res.string.profile_header_joined)
    val ago = stringResource(resource = Res.string.profile_header_ago)
    return when (this) {
        is MemberSinceState.Days -> {
            "$joined ${
                pluralStringResource(
                    resource = Res.plurals.profile_header_days_since,
                    quantity = days,
                    days,
                )
            } $ago"
        }

        is MemberSinceState.Months -> {
            "$joined ${
                pluralStringResource(
                    resource = Res.plurals.profile_header_months_since,
                    quantity = months,
                    months,
                )
            } $ago"
        }

        is MemberSinceState.Years -> {
            "$joined ${
                pluralStringResource(
                    resource = Res.plurals.profile_header_years_since,
                    quantity = years,
                    years,
                )
            } $ago"
        }

        is MemberSinceState.YearsAndMonths -> {
            "$joined ${
                pluralStringResource(
                    resource = Res.plurals.profile_header_years_since,
                    quantity = years,
                    years,
                )
            } ${
                pluralStringResource(
                    resource = Res.plurals.profile_header_months_since,
                    quantity = months,
                    months,
                )
            } $ago"
        }

        MemberSinceState.Unknown -> null
    }
}
