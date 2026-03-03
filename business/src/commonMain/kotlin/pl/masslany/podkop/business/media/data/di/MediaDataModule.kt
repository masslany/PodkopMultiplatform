package pl.masslany.podkop.business.media.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.media.data.main.MediaRepositoryImpl
import pl.masslany.podkop.business.media.domain.main.MediaRepository

val mediaDataModule = module {
    single<MediaRepository> {
        MediaRepositoryImpl(
            mediaDataSource = get(),
            dispatcherProvider = get(),
        )
    }
}
