package pl.masslany.podkop.business.accountsettings.data.network.client

import pl.masslany.podkop.business.accountsettings.data.network.api.AccountSettingsApi
import pl.masslany.podkop.business.accountsettings.data.network.models.GeneralSettingsResponseDto
import pl.masslany.podkop.common.network.api.ApiClient
import pl.masslany.podkop.common.network.api.request
import pl.masslany.podkop.common.network.models.request.Request

class AccountSettingsApiClient(
    private val apiClient: ApiClient,
) : AccountSettingsApi {
    override suspend fun getGeneralSettings(): Result<GeneralSettingsResponseDto> {
        val request = Request<GeneralSettingsResponseDto>(
            method = Request.HttpMethod.GET,
            path = "api/v3/settings/general",
        )

        return apiClient.request(request).fold(
            onSuccess = { Result.success(it.content) },
            onFailure = { Result.failure(it) },
        )
    }
}
