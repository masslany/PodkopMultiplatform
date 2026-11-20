package pl.masslany.podkop.features.tag

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class TagScreen(
    val tag: String,
    /** What the tag opens on; notifications about new entries or links open it on those, like website. */
    val content: TagContent = TagContent.All,
) : NavTarget

@Serializable
enum class TagContent {
    All,
    Entries,
    Links,
}
