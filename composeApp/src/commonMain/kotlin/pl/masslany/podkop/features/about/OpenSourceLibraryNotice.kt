package pl.masslany.podkop.features.about

data class OpenSourceLibraryNotice(
    val name: String,
    val artifact: String,
    val licenseName: String?,
    val licenseUrl: String?,
    val projectUrl: String?,
)
