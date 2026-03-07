package pl.masslany.podkop.features.privatemessages.preview

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import pl.masslany.podkop.common.composer.ComposerState
import pl.masslany.podkop.common.models.EmbedImageState
import pl.masslany.podkop.common.models.NameColorType
import pl.masslany.podkop.common.models.PublishedTimeType
import pl.masslany.podkop.common.models.avatar.AvatarState
import pl.masslany.podkop.common.models.avatar.AvatarType
import pl.masslany.podkop.common.models.avatar.GenderIndicatorType
import pl.masslany.podkop.features.privatemessages.models.ConversationMessageItemState
import pl.masslany.podkop.features.privatemessages.models.ConversationScreenState
import pl.masslany.podkop.features.privatemessages.models.InboxConversationItemState
import pl.masslany.podkop.features.privatemessages.models.NewConversationScreenState
import pl.masslany.podkop.features.privatemessages.models.PrivateMessageUserSuggestionItemState
import pl.masslany.podkop.features.privatemessages.models.PrivateMessagesScreenState
import pl.masslany.podkop.features.privatemessages.models.UserSuggestionsState
import pl.masslany.podkop.features.privatemessages.models.UserSuggestionsStatus

object PrivateMessagesPreviewFixtures {
    fun inboxState(): PrivateMessagesScreenState = PrivateMessagesScreenState(
        isLoading = false,
        isRefreshing = false,
        isError = false,
        isPaginating = false,
        shouldRequestNotificationPermission = false,
        conversations = listOf(
            InboxConversationItemState(
                username = "ZjemCiWanne",
                avatarState = AvatarState(
                    type = AvatarType.NoAvatar,
                    genderIndicatorType = GenderIndicatorType.Female,
                ),
                nameColorType = NameColorType.Orange,
                lastMessagePreview = "cat cat cat cat cat.",
                publishedAt = PublishedTimeType.HoursMinutes(hours = 1, minutes = 12),
                unread = true,
            ),
            InboxConversationItemState(
                username = "mirek_dev",
                avatarState = AvatarState(
                    type = AvatarType.NetworkImage("https://picsum.photos/seed/mirek/96/96"),
                    genderIndicatorType = GenderIndicatorType.Male,
                ),
                nameColorType = NameColorType.Green,
                lastMessagePreview = "dog dog dog dog bird.",
                publishedAt = PublishedTimeType.Days(days = 1),
                unread = false,
            ),
        ).toPersistentList(),
    )

    fun newConversationState(): NewConversationScreenState = NewConversationScreenState(
        username = "zje",
        suggestions = UserSuggestionsState(
            status = UserSuggestionsStatus.Content,
            items = persistentListOf(
                PrivateMessageUserSuggestionItemState(
                    username = "ZjemCiWanne",
                    avatarState = AvatarState(
                        type = AvatarType.NoAvatar,
                        genderIndicatorType = GenderIndicatorType.Female,
                    ),
                    nameColorType = NameColorType.Orange,
                ),
                PrivateMessageUserSuggestionItemState(
                    username = "testy",
                    avatarState = AvatarState(
                        type = AvatarType.NetworkImage("https://picsum.photos/seed/build/96/96"),
                        genderIndicatorType = GenderIndicatorType.Unspecified,
                    ),
                    nameColorType = NameColorType.Burgundy,
                ),
            ),
        ),
    )

    fun conversationState(): ConversationScreenState = ConversationScreenState(
        username = "ZjemCiWanne",
        isLoading = false,
        isRefreshing = false,
        isError = false,
        isPaginating = false,
        scrollToLatestMessage = 1,
        messages = listOf(
            ConversationMessageItemState(
                key = "1",
                isIncoming = true,
                contentState = null,
                embedImageState = null,
                embedUrl = null,
                adult = false,
                publishedAt = PublishedTimeType.HoursMinutes(hours = 2, minutes = 8),
                senderName = "ZjemCiWanne",
                senderAvatarState = AvatarState(
                    type = AvatarType.NoAvatar,
                    genderIndicatorType = GenderIndicatorType.Female,
                ),
                senderNameColorType = NameColorType.Orange,
            ),
            ConversationMessageItemState(
                key = "2",
                isIncoming = false,
                contentState = null,
                embedImageState = EmbedImageState(
                    url = "https://picsum.photos/seed/pm/800/600",
                    key = null,
                    source = "",
                    isAdult = false,
                    isGif = false,
                ),
                embedUrl = "https://www.youtube.com/watch?v=ZkXq44hGeEE",
                adult = false,
                publishedAt = PublishedTimeType.Minutes(4),
                senderName = null,
                senderAvatarState = AvatarState(
                    type = AvatarType.NoAvatar,
                    genderIndicatorType = GenderIndicatorType.Unspecified,
                ),
                senderNameColorType = NameColorType.Orange,
            ),
        ).toPersistentList(),
        composer = ComposerState.initial,
    )
}
