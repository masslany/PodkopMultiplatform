package pl.masslany.podkop.business.common.domain.models.common

sealed class Gender {
    data object Male : Gender()

    data object Female : Gender()

    data object Unspecified : Gender()
}
