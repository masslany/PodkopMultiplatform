package pl.masslany.podkop.features.privatemessages.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class PrivateMessagesScreenState(
    val isLoading: Boolean,
    val isRefreshing: Boolean,
    val isError: Boolean,
    val isPaginating: Boolean,
    val conversations: ImmutableList<InboxConversationItemState>,
) {
    companion object {
        val initial = PrivateMessagesScreenState(
            isLoading = true,
            isRefreshing = false,
            isError = false,
            isPaginating = false,
            conversations = persistentListOf(),
        )
    }
}
