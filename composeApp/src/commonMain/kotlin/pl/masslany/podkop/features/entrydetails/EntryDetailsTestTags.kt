package pl.masslany.podkop.features.entrydetails

object EntryDetailsTestTags {
    private const val Feature = "entry-details"

    object Screen {
        const val List = "$Feature:screen:list"
    }

    object Thread {
        fun moreReplies(parentId: Int): String = "$Feature:thread:more-replies:$parentId"
    }
}
