package pl.masslany.podkop.common.platform

interface AppMaintenanceController {
    val supportsCacheClearing: Boolean

    suspend fun clearCache(): Boolean
}
