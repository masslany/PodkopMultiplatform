package pl.masslany.podkop.business.accountsettings.data.main

import kotlinx.coroutines.withContext
import pl.masslany.podkop.business.accountsettings.data.api.AccountSettingsDataSource
import pl.masslany.podkop.business.accountsettings.domain.main.AccountSettingsRepository
import pl.masslany.podkop.business.accountsettings.domain.models.AccountContentSettings
import pl.masslany.podkop.common.coroutines.api.DispatcherProvider

class AccountSettingsRepositoryImpl(
    private val accountSettingsDataSource: AccountSettingsDataSource,
    private val dispatcherProvider: DispatcherProvider,
) : AccountSettingsRepository {
    override suspend fun getContentSettings() =
        withContext(dispatcherProvider.io) {
            accountSettingsDataSource.getGeneralSettings()
                .mapCatching { response -> AccountContentSettings(showAdult = response.data.show18) }
        }
}
