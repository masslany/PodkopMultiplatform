package pl.masslany.podkop.features.composer

import kotlinx.serialization.Serializable
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.common.navigation.NavTarget

@Serializable
data class ComposerBottomSheetScreen(val resultKey: String, val request: ComposerRequest) : NavTarget

@Serializable
data class ComposerPrefill(
    val content: String = "",
    val adult: Boolean = false,
    val photoKey: String? = null,
    val photoUrl: String? = null,
    val replyTarget: String? = null,
)

@Serializable
sealed interface ComposerRequest {
    val prefill: ComposerPrefill

    @Serializable
    data class CreateEntry(override val prefill: ComposerPrefill = ComposerPrefill()) : ComposerRequest

    @Serializable
    data class CreateEntryComment(val entryId: Int, override val prefill: ComposerPrefill = ComposerPrefill()) :
        ComposerRequest

    @Serializable
    data class CreateLinkComment(
        val linkId: Int,
        val parentCommentId: Int? = null,
        override val prefill: ComposerPrefill = ComposerPrefill(),
    ) : ComposerRequest

    @Serializable
    data class EditEntry(val entryId: Int, override val prefill: ComposerPrefill) : ComposerRequest

    @Serializable
    data class EditEntryComment(val entryId: Int, val commentId: Int, override val prefill: ComposerPrefill) :
        ComposerRequest

    @Serializable
    data class EditLinkComment(val linkId: Int, val commentId: Int, override val prefill: ComposerPrefill) :
        ComposerRequest
}

sealed interface ComposerResult {
    data class Submitted(val resource: ResourceItem) : ComposerResult

    data object Dismissed : ComposerResult
}
