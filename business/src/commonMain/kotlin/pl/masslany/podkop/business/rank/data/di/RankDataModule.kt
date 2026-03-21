package pl.masslany.podkop.business.rank.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.rank.data.main.RankRepositoryImpl
import pl.masslany.podkop.business.rank.domain.main.RankRepository

val rankDataModule = module {
    single<RankRepository> {
        RankRepositoryImpl(
            rankDataSource = get(),
            dispatcherProvider = get(),
        )
    }
}
