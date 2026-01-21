package pl.masslany.podkop.business.common.domain.models.common

sealed class Resource {
    data object Link : Resource()

    data object Entry : Resource()

    data object EntryComment : Resource()

    data object LinkComment : Resource()

    data object Unknown : Resource()
}
