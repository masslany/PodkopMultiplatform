package pl.masslany.podkop.features.links

object LinksTestTags {
    private const val Feature = "links"

    object Screen {
        const val List = "$Feature:screen:list"
    }

    object Item {
        fun link(id: Int): String = "$Feature:item:link:$id"
    }
}
