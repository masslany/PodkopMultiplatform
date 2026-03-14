package pl.masslany.podkop.common.platform

class IOSAppMaintenanceController : AppMaintenanceController {
    override val supportsCacheClearing: Boolean = false

    override suspend fun clearCache(): Boolean = false
}
