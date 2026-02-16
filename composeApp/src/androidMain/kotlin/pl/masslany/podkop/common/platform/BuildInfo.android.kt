package pl.masslany.podkop.common.platform

actual fun isDebugBuild(): Boolean = runCatching {
    val buildConfigClass = Class.forName("pl.masslany.podkop.BuildConfig")
    buildConfigClass.getField("DEBUG").getBoolean(null)
}.getOrDefault(false)
