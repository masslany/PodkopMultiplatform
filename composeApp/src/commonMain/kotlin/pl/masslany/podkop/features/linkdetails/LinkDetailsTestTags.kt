package pl.masslany.podkop.features.linkdetails

object LinkDetailsTestTags {
    private const val Feature = "link-details"

    object Screen {
        const val List = "$Feature:screen:list"
    }

    object Comment {
        fun showMoreReplies(commentId: Int): String = "$Feature:comment:show-more-replies:$commentId"
    }
}
