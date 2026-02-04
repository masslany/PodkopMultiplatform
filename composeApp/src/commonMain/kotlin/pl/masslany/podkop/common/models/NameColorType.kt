package pl.masslany.podkop.common.models

import pl.masslany.podkop.business.common.domain.models.common.NameColor

sealed class NameColorType {
    data object Orange : NameColorType()
    data object Burgundy : NameColorType()
    data object Green : NameColorType()
    data object Black : NameColorType()
}

fun NameColor.toNameColorType(): NameColorType = when (this) {
    NameColor.Orange -> NameColorType.Orange
    NameColor.Burgundy -> NameColorType.Burgundy
    NameColor.Green -> NameColorType.Green
    NameColor.Black -> NameColorType.Black
}
