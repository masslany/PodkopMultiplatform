package pl.masslany.podkop.business.embeds.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.embeds.data.main.StreamableVideoRepositoryImpl
import pl.masslany.podkop.business.embeds.domain.main.StreamableVideoRepository

val streamableVideoDataModule = module {
    single<StreamableVideoRepository> {
        StreamableVideoRepositoryImpl(
            dataSource = get(),
            dispatcherProvider = get(),
        )
    }
}
