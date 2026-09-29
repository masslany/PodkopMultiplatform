package pl.masslany.podkop.business.accountsettings.domain.main

import pl.masslany.podkop.business.accountsettings.domain.models.AccountContentSettings

interface AccountSettingsRepository {
    suspend fun getContentSettings(): Result<AccountContentSettings>
}
