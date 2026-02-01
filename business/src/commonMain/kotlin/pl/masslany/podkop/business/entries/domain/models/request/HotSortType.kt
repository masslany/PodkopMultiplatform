package pl.masslany.podkop.business.entries.domain.models.request

sealed class HotSortType(val value: Int) {
    data object TwoHours : HotSortType(TWO_HOURS)

    data object SixHours : HotSortType(SIX_HOURS)

    data object TwelveHours : HotSortType(TWELVE_HOURS)

    private companion object {
        const val TWO_HOURS = 2
        const val SIX_HOURS = 6
        const val TWELVE_HOURS = 12
    }
}
