package pl.masslany.podkop.features.hits.archivepicker

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.StringResource
import podkop.composeapp.generated.resources.Res
import podkop.composeapp.generated.resources.hits_archive_button

private const val ArchiveStartYear = 2007
private const val ArchiveStartMonth = 12

data class HitsArchivePickerState(
    val selectedYear: Int,
    val selectedMonth: Int,
    val minYear: Int,
    val maxYear: Int,
    val maxMonthInMaxYear: Int,
    val months: ImmutableList<HitsArchiveMonthState>,
) {
    val isPreviousYearEnabled: Boolean = selectedYear > minYear
    val isNextYearEnabled: Boolean = selectedYear < maxYear

    fun selectPreviousYear(): HitsArchivePickerState = updateYear(selectedYear - 1)

    fun selectNextYear(): HitsArchivePickerState = updateYear(selectedYear + 1)

    fun selectMonth(month: Int): HitsArchivePickerState = copy(
        selectedMonth = month,
        months = buildMonths(
            selectedYear = selectedYear,
            selectedMonth = month,
            maxYear = maxYear,
            maxMonth = maxMonthInMaxYear,
        ),
    )

    fun toArchiveState(): HitsArchiveState = HitsArchiveState(
        year = selectedYear,
        month = selectedMonth,
    )

    private fun updateYear(year: Int): HitsArchivePickerState {
        val normalizedYear = year.coerceIn(minYear, maxYear)
        val normalizedMonth = normalizeMonth(
            year = normalizedYear,
            month = selectedMonth,
            maxYear = maxYear,
            maxMonth = maxMonthInMaxYear,
        )
        return copy(
            selectedYear = normalizedYear,
            selectedMonth = normalizedMonth,
            months = buildMonths(
                selectedYear = normalizedYear,
                selectedMonth = normalizedMonth,
                maxYear = maxYear,
                maxMonth = maxMonthInMaxYear,
            ),
        )
    }

    companion object {
        val archiveButtonLabelRes: StringResource = Res.string.hits_archive_button

        fun create(
            selectedArchive: HitsArchiveState?,
            currentYear: Int,
            currentMonth: Int,
        ): HitsArchivePickerState {
            val initialArchive = selectedArchive ?: HitsArchiveState(
                year = currentYear,
                month = currentMonth,
            )
            val normalizedYear = initialArchive.year.coerceIn(ArchiveStartYear, currentYear)
            val normalizedMonth = normalizeMonth(
                year = normalizedYear,
                month = initialArchive.month,
                maxYear = currentYear,
                maxMonth = currentMonth,
            )
            return HitsArchivePickerState(
                selectedYear = normalizedYear,
                selectedMonth = normalizedMonth,
                minYear = ArchiveStartYear,
                maxYear = currentYear,
                maxMonthInMaxYear = currentMonth,
                months = buildMonths(
                    selectedYear = normalizedYear,
                    selectedMonth = normalizedMonth,
                    maxYear = currentYear,
                    maxMonth = currentMonth,
                ),
            )
        }

        private fun buildMonths(
            selectedYear: Int,
            selectedMonth: Int,
            maxYear: Int,
            maxMonth: Int,
        ): ImmutableList<HitsArchiveMonthState> = (1..12).map { month ->
            HitsArchiveMonthState(
                month = month,
                isEnabled = isMonthEnabled(
                    year = selectedYear,
                    month = month,
                    maxYear = maxYear,
                    maxMonth = maxMonth,
                ),
                isSelected = selectedMonth == month,
            )
        }.toImmutableList()

        private fun normalizeMonth(
            year: Int,
            month: Int,
            maxYear: Int,
            maxMonth: Int,
        ): Int {
            val enabledMonths = (1..12).filter { candidate ->
                isMonthEnabled(
                    year = year,
                    month = candidate,
                    maxYear = maxYear,
                    maxMonth = maxMonth,
                )
            }
            return if (month in enabledMonths) month else enabledMonths.first()
        }

        private fun isMonthEnabled(
            year: Int,
            month: Int,
            maxYear: Int,
            maxMonth: Int,
        ): Boolean {
            if (year == ArchiveStartYear && month < ArchiveStartMonth) {
                return false
            }
            if (year == maxYear && month > maxMonth) {
                return false
            }
            return true
        }
    }
}
