package pl.masslany.podkop.business.tags.data.di

import org.koin.dsl.module
import pl.masslany.podkop.business.tags.data.main.TagsRepositoryImpl
import pl.masslany.podkop.business.tags.domain.main.TagsRepository

val tagsDataModule = module {
    single<TagsRepository> {
        TagsRepositoryImpl(
            tagsDataSource = get(),
            dispatcherProvider = get(),
        )
    }
}
