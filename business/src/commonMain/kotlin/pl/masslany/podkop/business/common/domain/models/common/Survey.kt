package pl.masslany.podkop.business.common.domain.models.common

data class Survey(
    val actions: Actions,
    val answers: List<Answer>,
    val count: Int,
    val deletable: Boolean,
    val editable: Boolean,
    val key: String,
    val question: String,
    val voted: Int,
)
