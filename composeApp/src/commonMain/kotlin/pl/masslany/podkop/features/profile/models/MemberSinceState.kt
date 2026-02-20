package pl.masslany.podkop.features.profile.models

sealed class MemberSinceState {
    data class Days(val days: Int) : MemberSinceState()

    data class Months(val months: Int) : MemberSinceState()

    data class Years(val years: Int) : MemberSinceState()

    data class YearsAndMonths(val years: Int, val months: Int) : MemberSinceState()

    data object Unknown : MemberSinceState()
}
