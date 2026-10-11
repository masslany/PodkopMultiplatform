package pl.masslany.podkop.common.components

/** Test tags of components shared by many screens. */
object CommonTestTags {
    private const val Feature = "common"

    object Error {
        const val Screen = "$Feature:error:screen"
        const val Retry = "$Feature:error:retry"
    }

    object Pagination {
        const val Retry = "$Feature:pagination:retry"
    }
}
