package pl.masslany.podkop.features.privatemessages.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.privatemessages.ConversationScreen
import pl.masslany.podkop.features.privatemessages.conversation.ConversationViewModel
import pl.masslany.podkop.features.privatemessages.inbox.PrivateMessagesViewModel
import pl.masslany.podkop.features.privatemessages.newconversation.NewConversationViewModel

val privateMessagesModule = module {
    viewModel {
        PrivateMessagesViewModel(
            privateMessagesRepository = get(),
            appNavigator = get(),
            logger = get(),
            snackbarManager = get(),
        )
    }

    viewModel {
        NewConversationViewModel(
            profileRepository = get(),
            appNavigator = get(),
            savedStateHandle = get(),
            logger = get(),
        )
    }

    viewModel { params ->
        ConversationViewModel(
            screen = params.get<ConversationScreen>(),
            privateMessagesRepository = get(),
            notificationsRepository = get(),
            mediaRepository = get(),
            appNavigator = get(),
            savedStateHandle = get(),
            logger = get(),
            snackbarManager = get(),
        )
    }
}
