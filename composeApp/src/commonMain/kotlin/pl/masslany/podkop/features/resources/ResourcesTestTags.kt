package pl.masslany.podkop.features.resources

/** Test tags of the link, entry and comment items that many screens list. */
object ResourcesTestTags {
    private const val Feature = "resources"

    object Link {
        fun vote(id: Int): String = "$Feature:link:vote:$id"
    }
}
