package pl.masslany.podkop.business.common.data.main.mapper

import pl.masslany.podkop.business.common.domain.models.common.Gender


fun String?.toGender(): Gender {
    return when (this) {
        "m" -> Gender.Male
        "f" -> Gender.Female
        else -> Gender.Unspecified
    }
}
