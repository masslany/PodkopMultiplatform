package pl.masslany.podkop.business.common.domain.models.common

sealed class Deleted {
    data object Moderator : Deleted()

    data object Author : Deleted()

    data object Host : Deleted()

    data object None : Deleted()
}
