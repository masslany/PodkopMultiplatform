package pl.masslany.podkop.business.blacklists.domain.models

/** Normalizes user-typed blacklist values the same way on every platform. */
object BlacklistNormalization {
    fun user(value: String): String = value.trim().removePrefix("@")

    fun tag(value: String): String = value.trim().removePrefix("#").lowercase()

    fun domain(value: String): String = value.trim().lowercase()
}
