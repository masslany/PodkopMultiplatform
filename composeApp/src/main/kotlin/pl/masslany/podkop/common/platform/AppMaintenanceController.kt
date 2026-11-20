package pl.masslany.podkop.common.platform

interface AppMaintenanceController {
    suspend fun clearCache(): Boolean
}
