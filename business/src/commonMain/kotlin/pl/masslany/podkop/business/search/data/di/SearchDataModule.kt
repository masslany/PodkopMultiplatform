package pl.masslany.podkop.business.search.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.search.data.main.SearchRepositoryImpl
import pl.masslany.podkop.business.search.domain.main.SearchRepository

val searchDataModule = module {
    factory<SearchRepository> {
        SearchRepositoryImpl(
            searchDataSource = get(),
            dispatcherProvider = get(),
        )
    }
}
