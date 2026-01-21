package pl.masslany.podkop.business.common.domain.models.common

sealed class NameColor {
    data object Orange : NameColor()

    data object Burgundy : NameColor()

    data object Green : NameColor()

    data object Black : NameColor()
}
