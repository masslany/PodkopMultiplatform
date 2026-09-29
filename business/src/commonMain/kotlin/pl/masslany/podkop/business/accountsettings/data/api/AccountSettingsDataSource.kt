package pl.masslany.podkop.business.accountsettings.data.api

import pl.masslany.podkop.business.accountsettings.data.network.models.GeneralSettingsResponseDto

interface AccountSettingsDataSource {
    suspend fun getGeneralSettings(): Result<GeneralSettingsResponseDto>
}
