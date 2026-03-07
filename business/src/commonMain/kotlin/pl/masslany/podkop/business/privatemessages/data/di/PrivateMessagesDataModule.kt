package pl.masslany.podkop.business.privatemessages.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.privatemessages.data.main.PrivateMessagesRepositoryImpl
import pl.masslany.podkop.business.privatemessages.domain.main.PrivateMessagesRepository

val privateMessagesDataModule = module {
    single<PrivateMessagesRepository> {
        PrivateMessagesRepositoryImpl(
            privateMessagesDataSource = get(),
            dispatcherProvider = get(),
        )
    }
}
