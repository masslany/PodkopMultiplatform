package pl.masslany.podkop.common.models.avatar

import pl.masslany.podkop.business.common.domain.models.common.Gender

sealed class GenderIndicatorType {
    data object Male : GenderIndicatorType()
    data object Female : GenderIndicatorType()
    data object Unspecified : GenderIndicatorType()
}

fun Gender.toGenderIndicatorType(): GenderIndicatorType =
    when (this) {
        Gender.Male -> GenderIndicatorType.Male
        Gender.Female -> GenderIndicatorType.Female
        Gender.Unspecified -> GenderIndicatorType.Unspecified
    }
