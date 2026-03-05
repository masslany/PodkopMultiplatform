package pl.masslany.podkop.features.resourceactions

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem

class ResourceActionUpdatesStore {
    private val _updates = MutableSharedFlow<ResourceActionUpdate>(extraBufferCapacity = 32)
    val updates = _updates.asSharedFlow()

    fun tryEmit(update: ResourceActionUpdate) {
        _updates.tryEmit(update)
    }
}

sealed interface ResourceActionUpdate {
    data class EntryCommentDeleted(val commentId: Int) : ResourceActionUpdate
    data class ResourceEdited(val resource: ResourceItem) : ResourceActionUpdate
}
