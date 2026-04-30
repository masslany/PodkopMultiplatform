package pl.masslany.podkop.business.notifications.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.notifications.data.main.NotificationsRepositoryImpl
import pl.masslany.podkop.business.notifications.domain.main.NotificationsRepository

val notificationsDataModule = module {
    single<NotificationsRepository> {
        NotificationsRepositoryImpl(
            notificationsDataSource = get(),
            privateMessagesDataSource = get(),
            authRepository = get(),
            dispatcherProvider = get(),
            appScope = get(),
            logger = get(),
        )
    }
}
