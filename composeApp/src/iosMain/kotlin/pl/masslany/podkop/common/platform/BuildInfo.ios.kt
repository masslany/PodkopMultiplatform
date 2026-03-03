package pl.masslany.podkop.common.platform

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import platform.Foundation.NSBundle

@OptIn(ExperimentalNativeApi::class)
actual fun isDebugBuild(): Boolean = Platform.isDebugBinary

actual fun appVersionName(): String {
    val bundle = NSBundle.mainBundle
    val shortVersion = bundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
    val buildVersion = bundle.objectForInfoDictionaryKey("CFBundleVersion") as? String

    return when {
        !shortVersion.isNullOrBlank() && !buildVersion.isNullOrBlank() && shortVersion != buildVersion ->
            "$shortVersion ($buildVersion)"

        !shortVersion.isNullOrBlank() -> shortVersion
        !buildVersion.isNullOrBlank() -> buildVersion
        else -> "unknown"
    }
}
