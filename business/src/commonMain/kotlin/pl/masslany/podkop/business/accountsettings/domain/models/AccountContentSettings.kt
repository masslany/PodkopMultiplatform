package pl.masslany.podkop.business.accountsettings.domain.models

/** The signed-in user's content preferences from Wykop's account settings on the website. */
data class AccountContentSettings(
    val showAdult: Boolean,
)
