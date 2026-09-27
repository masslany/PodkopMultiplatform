package pl.masslany.podkop.ios

import pl.masslany.podkop.ios.generated.GeneratedOpenSourceLibraries

/** An open source library linked into the iOS framework, with its POM license metadata. */
class IOSLibraryNotice(
    val name: String,
    val artifact: String,
    val licenseName: String?,
    val licenseUrl: String?,
    val projectUrl: String?,
)

class AboutService internal constructor() {
    /** Libraries resolved into PodkopShared at build time, sorted by name. */
    fun libraries(): List<IOSLibraryNotice> = GeneratedOpenSourceLibraries
}
