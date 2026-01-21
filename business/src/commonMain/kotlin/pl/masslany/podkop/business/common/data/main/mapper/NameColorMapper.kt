package pl.masslany.podkop.business.common.data.main.mapper

import pl.masslany.podkop.business.common.domain.models.common.NameColor

fun String.toNameColor(): NameColor {
    return when (this) {
        "orange" -> NameColor.Orange
        "burgundy" -> NameColor.Burgundy
        "green" -> NameColor.Green
        "black" -> NameColor.Black
        else -> NameColor.Orange
    }
}
